package us.tastybento.bskyblock.api.user;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Utilities class that helps to avoid spamming the User with potential repeated messages
 * @author Poslovitch
 */
public class Notifier {

    /**
     * Time in seconds before {@link #notificationCache} removes the entry related to the player.
     */
    private final int NOTIFICATION_DELAY = 5;

    private final LoadingCache<User, Notification> notificationCache = CacheBuilder.newBuilder()
            .expireAfterAccess(NOTIFICATION_DELAY, TimeUnit.SECONDS)
            .maximumSize(500)
            .build(
                    new CacheLoader<User, Notification>() {
                        @Override
                        public Notification load(User user) throws Exception {
                            return new Notification(null, 0);
                        }
                    }
            );

    public synchronized boolean notify(User user, String message) {
        try {
            Notification lastNotification = notificationCache.get(user);
            long now = System.currentTimeMillis();
            if (now >= lastNotification.getTime() + NOTIFICATION_DELAY * 1000 || !message.equals(lastNotification.getMessage())) {
                notificationCache.put(user, new Notification(message, now));
                user.sendRawMessage(message);
            }
            return true;
        } catch (ExecutionException e) {
            return false;
        }
    }

    public class Notification {
        private final String message;
        private final long time;

        public Notification(String message, long time) {
            this.message = message;
            this.time = time;
        }

        public String getMessage() {
            return message;
        }

        public long getTime() {
            return time;
        }
    }
}