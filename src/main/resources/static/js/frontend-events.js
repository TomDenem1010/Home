(() => {
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

    function showNotification(notification) {
        const container = document.querySelector("#notification-container");
        const toast = document.createElement("div");
        const type = notification.type.toLowerCase();
        const presentation = {
            success: {icon: "✓", title: "Operation completed"},
            warning: {icon: "!", title: "Warning"},
            error: {icon: "×", title: "An error occurred"}
        }[type];
        const lifetime = 5000;

        toast.className = `notification notification--${type}`;
        toast.setAttribute("role", type === "error" ? "alert" : "status");
        toast.style.setProperty("--notification-lifetime", `${lifetime}ms`);

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
            toast.classList.add("notification--leaving");
            toast.addEventListener("transitionend", () => toast.remove(), {once: true});
            window.setTimeout(() => toast.remove(), 300);
        };

        closeButton.addEventListener("click", dismiss);
        window.setTimeout(dismiss, lifetime);
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
