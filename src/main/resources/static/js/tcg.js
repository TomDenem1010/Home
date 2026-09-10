(() => {
    initializeSortableTables();
    initializeActions();
    initializeNotifications();

    function initializeSortableTables() {
        document.querySelectorAll("[data-sortable-table]").forEach(table => {
            const headers = [...table.querySelectorAll("thead th")];
            const body = table.tBodies[0];
            headers.forEach((header, columnIndex) => {
                const button = header.querySelector(".sort-button");
                if (!button) return;
                button.addEventListener("click", () => {
                    const ascending = header.getAttribute("aria-sort") !== "ascending";
                    const sortType = header.dataset.sortType || "text";
                    const rows = [...body.rows];
                    rows.sort((leftRow, rightRow) => {
                        const comparison = compare(
                            sortValue(leftRow.cells[columnIndex]),
                            sortValue(rightRow.cells[columnIndex]),
                            sortType
                        );
                        return ascending ? comparison : -comparison;
                    });
                    headers.forEach(otherHeader => otherHeader.removeAttribute("aria-sort"));
                    header.setAttribute("aria-sort", ascending ? "ascending" : "descending");
                    rows.forEach(row => body.appendChild(row));
                });
            });
        });
    }

    function initializeActions() {
        document.querySelectorAll(".tcg-action-form").forEach(form => {
            form.addEventListener("submit", async event => {
                event.preventDefault();
                const submitButton = form.querySelector('button[type="submit"]');
                submitButton?.setAttribute("disabled", "disabled");
                try {
                    const response = await fetch(form.action, {method: form.method, body: new FormData(form)});
                    if (!response.ok) throw new Error(`TCG action failed with status ${response.status}`);
                } catch (error) {
                    showNotification({type: "ERROR", message: "The TCG operation could not be started."});
                    console.error(error);
                } finally {
                    submitButton?.removeAttribute("disabled");
                }
            });
        });
    }

    function initializeNotifications() {
        restoreNotifications();
        const source = new EventSource("/api/frontend-events");
        source.addEventListener("notification", event => showNotification(JSON.parse(event.data)));
        source.onerror = () => source.close();
    }

    function showNotification(notification, stored = null) {
        const presentation = {
            success: {icon: "✓", title: "Operation completed"},
            warning: {icon: "!", title: "Warning"},
            error: {icon: "×", title: "An error occurred"}
        }[notification.type.toLowerCase()];
        if (!presentation) return;

        const lifetime = 5000;
        const record = stored ?? {
            id: globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`,
            notification,
            expiresAt: Date.now() + lifetime
        };
        const remainingLifetime = record.expiresAt - Date.now();
        if (remainingLifetime <= 0) {
            removeStoredNotification(record.id);
            return;
        }
        if (!stored) writeStoredNotifications([...readStoredNotifications(), record]);

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

    function sortValue(cell) {
        return cell.dataset.sortValue ?? cell.textContent.trim();
    }

    function compare(left, right, sortType) {
        const leftMissing = left === "" || left === null;
        const rightMissing = right === "" || right === null;
        if (leftMissing || rightMissing) return leftMissing === rightMissing ? 0 : leftMissing ? 1 : -1;
        if (sortType === "number") return Number(left) - Number(right);
        if (sortType === "date") return Date.parse(left) - Date.parse(right);
        return left.localeCompare(right, "hu", {numeric: true, sensitivity: "base"});
    }

    function restoreNotifications() {
        readStoredNotifications().forEach(record => showNotification(record.notification, record));
    }

    function readStoredNotifications() {
        try {
            const records = JSON.parse(sessionStorage.getItem("home.tcg.notifications") ?? "[]");
            return Array.isArray(records) ? records : [];
        } catch {
            return [];
        }
    }

    function writeStoredNotifications(records) {
        try {
            sessionStorage.setItem("home.tcg.notifications", JSON.stringify(records));
        } catch {
            // Notifications still work while storage is unavailable.
        }
    }

    function removeStoredNotification(id) {
        writeStoredNotifications(readStoredNotifications().filter(record => record.id !== id));
    }
})();
