(() => {
    const storageKey = "home.frontend-notifications";
    const notificationLifetime = 5000;

    window.showFrontendNotification = showNotification;
    restoreNotifications();

    const source = new EventSource("/api/frontend-events");
    source.addEventListener("notification", event => showNotification(JSON.parse(event.data)));
    source.onerror = () => {
        // EventSource reconnects automatically after temporary connection failures.
    };

    function showNotification(notification, storedNotification = null) {
        const presentation = {
            success: {icon: "✓", title: "Operation completed"},
            warning: {icon: "!", title: "Warning"},
            error: {icon: "×", title: "An error occurred"}
        }[notification.type.toLowerCase()];
        if (!presentation) return;

        const record = storedNotification ?? {
            id: globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`,
            notification,
            expiresAt: Date.now() + notificationLifetime
        };
        const remainingLifetime = record.expiresAt - Date.now();
        if (remainingLifetime <= 0) {
            removeStoredNotification(record.id);
            return;
        }
        if (!storedNotification) writeStoredNotifications([...readStoredNotifications(), record]);

        const toast = document.createElement("div");
        toast.className = `notification notification--${notification.type.toLowerCase()}`;
        toast.setAttribute("role", notification.type === "ERROR" ? "alert" : "status");
        toast.style.setProperty("--notification-lifetime", `${remainingLifetime}ms`);
        const icon = document.createElement("span");
        icon.className = "notification__icon";
        icon.setAttribute("aria-hidden", "true");
        icon.textContent = presentation.icon;
        const content = document.createElement("div");
        content.className = "notification__content";
        const title = document.createElement("strong");
        title.className = "notification__title";
        title.textContent = presentation.title;
        const message = document.createElement("span");
        message.className = "notification__message";
        message.textContent = notification.message;
        const closeButton = document.createElement("button");
        closeButton.className = "notification__close";
        closeButton.type = "button";
        closeButton.setAttribute("aria-label", "Dismiss notification");
        closeButton.textContent = "×";
        const progress = document.createElement("span");
        progress.className = "notification__progress";
        progress.setAttribute("aria-hidden", "true");
        content.append(title, message);
        toast.append(icon, content, closeButton, progress);
        document.querySelector("#notification-container")?.append(toast);

        const dismiss = () => {
            if (toast.classList.contains("notification--leaving")) return;
            removeStoredNotification(record.id);
            toast.classList.add("notification--leaving");
            window.setTimeout(() => toast.remove(), 300);
        };
        closeButton.addEventListener("click", dismiss);
        window.setTimeout(dismiss, remainingLifetime);
    }

    function restoreNotifications() {
        readStoredNotifications().forEach(record => showNotification(record.notification, record));
    }
    function readStoredNotifications() {
        try {
            const records = JSON.parse(sessionStorage.getItem(storageKey) ?? "[]");
            return Array.isArray(records) ? records : [];
        } catch {
            return [];
        }
    }
    function writeStoredNotifications(records) {
        try {
            sessionStorage.setItem(storageKey, JSON.stringify(records));
        } catch {
            // Notifications still work while storage is unavailable.
        }
    }
    function removeStoredNotification(id) {
        writeStoredNotifications(readStoredNotifications().filter(record => record.id !== id));
    }
})();
