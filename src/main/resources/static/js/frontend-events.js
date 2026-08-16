(() => {
    const storageKey = "home.frontend-notifications";
    const notificationLifetime = 5000;
    const source = new EventSource("/api/frontend-events");

    source.onmessage = event => dispatch("message", event);
    source.onerror = () => source.close();

    window.addFrontendEventListener = (type, listener) => {
        source.addEventListener(type, event => {
            dispatch(type, event);
            listener(JSON.parse(event.data));
        });
    };

    window.addFrontendEventListener("notification", showNotification);
    initializeActionForms();
    restoreNotifications();

    function initializeActionForms() {
        document.querySelectorAll(".frontend-action-form").forEach(form => {
            form.addEventListener("submit", async event => {
                event.preventDefault();
                const submitButton = form.querySelector('button[type="submit"]');
                submitButton?.setAttribute("disabled", "disabled");
                try {
                    const response = await fetch(form.action, {
                        method: form.method,
                        body: new FormData(form)
                    });
                    if (!response.ok) {
                        throw new Error(`Action failed with status ${response.status}`);
                    }
                } catch (error) {
                    showNotification({type: "ERROR", message: "The operation could not be started."});
                    console.error(error);
                } finally {
                    submitButton?.removeAttribute("disabled");
                }
            });
        });
    }

    function showNotification(notification, context = {}) {
        const storedNotification = context.storedNotification;
        const container = document.querySelector("#notification-container");
        const toast = document.createElement("div");
        const type = notification.type.toLowerCase();
        const presentation = {
            success: {icon: "✓", title: "Operation completed"},
            warning: {icon: "!", title: "Warning"},
            error: {icon: "×", title: "An error occurred"}
        }[type];
        const id = storedNotification?.id ?? createNotificationId();
        const lifetime = storedNotification?.lifetime ?? notificationLifetime;
        const expiresAt = storedNotification?.expiresAt ?? Date.now() + lifetime;
        const remainingLifetime = expiresAt - Date.now();

        if (!presentation || remainingLifetime <= 0) {
            removeStoredNotification(id);
            return;
        }
        if (!storedNotification) {
            storeNotification({id, notification, expiresAt, lifetime});
        }

        toast.className = `notification notification--${type}`;
        toast.setAttribute("role", type === "error" ? "alert" : "status");
        toast.style.setProperty("--notification-lifetime", `${remainingLifetime}ms`);
        toast.style.setProperty(
            "--notification-progress-width",
            `${Math.min(100, remainingLifetime / lifetime * 100)}%`
        );

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
        container.append(toast);
        const dismiss = () => {
            if (toast.classList.contains("notification--leaving")) {
                return;
            }
            removeStoredNotification(id);
            toast.classList.add("notification--leaving");
            toast.addEventListener("transitionend", () => toast.remove(), {once: true});
            window.setTimeout(() => toast.remove(), 300);
        };

        closeButton.addEventListener("click", dismiss);
        window.setTimeout(dismiss, remainingLifetime);
    }

    function restoreNotifications() {
        readStoredNotifications().forEach(storedNotification => {
            showNotification(storedNotification.notification, {storedNotification});
        });
    }

    function storeNotification(storedNotification) {
        writeStoredNotifications([...readStoredNotifications(), storedNotification]);
    }

    function removeStoredNotification(id) {
        writeStoredNotifications(readStoredNotifications().filter(notification => notification.id !== id));
    }

    function readStoredNotifications() {
        try {
            const notifications = JSON.parse(sessionStorage.getItem(storageKey) ?? "[]");
            return Array.isArray(notifications) ? notifications : [];
        } catch {
            return [];
        }
    }

    function writeStoredNotifications(notifications) {
        try {
            sessionStorage.setItem(storageKey, JSON.stringify(notifications));
        } catch {
            // Notifications still work for the current page when storage is unavailable.
        }
    }

    function createNotificationId() {
        return globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`;
    }

    function dispatch(type, event) {
        if (!event.data) {
            return;
        }
        window.dispatchEvent(new CustomEvent("home:frontend-event", {
            detail: {type, event: JSON.parse(event.data)}
        }));
    }
})();
