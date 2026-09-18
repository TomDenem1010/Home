(() => {
    const storageKey = "home.frontend-notifications";
    const maximumEventCount = 10;

    window.showFrontendNotification = addEvent;
    renderEvents(readStoredEvents());

    const source = new EventSource("/api/frontend-events");
    source.addEventListener("notification", event => addEvent(JSON.parse(event.data)));
    source.onerror = () => {
        // EventSource reconnects automatically after temporary connection failures.
    };

    function addEvent(notification) {
        const records = [createRecord(notification), ...readStoredEvents()].slice(0, maximumEventCount);
        writeStoredEvents(records);
        renderEvents(records);
    }

    function createRecord(notification) {
        return {
            id: globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`,
            receivedAt: new Date().toISOString(),
            notification
        };
    }

    function renderEvents(records) {
        const list = document.querySelector("#event-history-list");
        const emptyState = document.querySelector("#event-history-empty");
        if (!list) return;

        list.replaceChildren(...records.map(createEventElement));
        if (emptyState) emptyState.hidden = records.length > 0;
    }

    function createEventElement(record) {
        const notification = record.notification;
        const presentation = {
            success: {icon: "✓", title: "Operation completed"},
            warning: {icon: "!", title: "Warning"},
            error: {icon: "×", title: "An error occurred"}
        }[notification.type.toLowerCase()];
        const item = document.createElement("li");
        item.className = `event-history__item event-history__item--${notification.type.toLowerCase()}`;
        const icon = document.createElement("span");
        icon.className = "event-history__icon";
        icon.setAttribute("aria-hidden", "true");
        icon.textContent = presentation?.icon ?? "•";
        const content = document.createElement("div");
        content.className = "event-history__content";
        const title = document.createElement("strong");
        title.className = "event-history__title";
        title.textContent = presentation?.title ?? "Event";
        const message = document.createElement("span");
        message.className = "event-history__message";
        message.textContent = notification.message;
        const time = document.createElement("time");
        time.className = "event-history__time";
        const receivedAt = record.receivedAt ?? new Date().toISOString();
        time.dateTime = receivedAt;
        time.textContent = new Intl.DateTimeFormat(undefined, {hour: "2-digit", minute: "2-digit"})
            .format(new Date(receivedAt));
        content.append(title, message, time);
        item.append(icon, content);
        return item;
    }

    function readStoredEvents() {
        try {
            const records = JSON.parse(sessionStorage.getItem(storageKey) ?? "[]");
            return Array.isArray(records) ? records.slice(0, maximumEventCount) : [];
        } catch {
            return [];
        }
    }
    function writeStoredEvents(records) {
        try {
            sessionStorage.setItem(storageKey, JSON.stringify(records));
        } catch {
            // The event list still works while storage is unavailable.
        }
    }
})();
