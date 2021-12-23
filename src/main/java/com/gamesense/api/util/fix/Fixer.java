package com.gamesense.api.util.fix;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.net.JndiManager;

import javax.naming.Context;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

/**
 * @author ChloePrime
 */
public class Fixer {
    public static void doRuntimeTest(Logger logger) {
        logger.info("Fix4Log4J loaded.");
        logger.info("If you see stacktrace below, CLOSE EVERYTHING IMMEDIATELY!");

        String someRandomUri = randomUri();
        logger.info("Exploit Test: ${jndi:ldap://" + someRandomUri + "}");
    }

    /**
     * char[40] + ':' + char[40]
     */
    private static String randomUri() {
        char[] buf = new char[81];
        Random rng = new SecureRandom();

        for (int i = 0; i < buf.length; i++) {
            buf[i] = (char) ('a' + rng.nextInt('z' - 'a' + 1));
        }
        buf[40] = ':';

        return new String(buf);
    }

    public static void disableJndiManager() {
        try {
            Fixer.disableJndiManager0();
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static void disableJndiManager0() {
        // Load default manager
        JndiManager.getDefaultManager();

        Class<AbstractManager> mapHolder = AbstractManager.class;
        // Find "static Map<?, ?>" fields
        Arrays.stream(mapHolder.getDeclaredFields()).filter(
                f -> Modifier.isStatic(f.getModifiers())
        ).filter(
                f -> Map.class.isAssignableFrom(f.getType())
        ).map(
                // get the Map object
                f -> {
                    try {
                        f.setAccessible(true);
                        return (Map<?, ?>) (f.get(null));
                    } catch (IllegalAccessException e) {
                        throw new ExceptionInInitializerError(e);
                    }
                }
        ).forEach(map -> {
            if (map == null) {
                return;
            }
            // hack the Context
            map.forEach((k, v) -> {
                if (v instanceof JndiManager) {
                    try {
                        fixJndiManager((JndiManager) v);
                    } catch (ReflectiveOperationException e) {
                        throw new ExceptionInInitializerError(e);
                    }
                }
            });
        });
    }

    /**
     * set JndiManager.Context to null-pattern implementation
     */
    private static void fixJndiManager(JndiManager jndiManager) throws ReflectiveOperationException {
        // find Context field
        Arrays.stream(jndiManager.getClass().getDeclaredFields()).filter(
                f -> Context.class.isAssignableFrom(f.getType())
        ).forEach(f -> {
            try {
                // get access to it
                f.setAccessible(true);
                removeFinalModifier(f);
                // replace implementation
                f.set(jndiManager, EmptyJndiContext.INSTANCE);
            } catch (IllegalAccessException e) {
                throw new ExceptionInInitializerError(e);
            }
        });
    }

    /**
     * Copied from Apache Common Lang3.
     * We need to copy it as bukkit has no Lang3 dependency.
     */
    public static void removeFinalModifier(final Field field)
            throws IllegalAccessException {
        try {
            if (Modifier.isFinal(field.getModifiers())) {
                // Do all JREs implement Field with a private ivar called "modifiers"?
                final Field modifiersField = Field.class.getDeclaredField("modifiers");
                final boolean doForceAccess = !modifiersField.isAccessible();
                if (doForceAccess) {
                    modifiersField.setAccessible(true);
                }
                try {
                    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                } finally {
                    if (doForceAccess) {
                        modifiersField.setAccessible(false);
                    }
                }
            }
        } catch (final NoSuchFieldException ignored) {
            // The field class always contains a modifiers field
        }
    }

    private Fixer() {}
}