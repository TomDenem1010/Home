(() => {
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
                    const comparison = compare(sortValue(leftRow.cells[columnIndex]), sortValue(rightRow.cells[columnIndex]), sortType);
                    return ascending ? comparison : -comparison;
                });
                headers.forEach(otherHeader => otherHeader.removeAttribute("aria-sort"));
                header.setAttribute("aria-sort", ascending ? "ascending" : "descending");
                rows.forEach(row => body.appendChild(row));
            });
        });
    });

    document.querySelectorAll(".tcg-action-form").forEach(form => {
        form.addEventListener("submit", async event => {
            event.preventDefault();
            const submitButton = form.querySelector('button[type="submit"]');
            submitButton?.setAttribute("disabled", "disabled");
            try {
                const response = await fetch(form.action, {method: form.method, body: new FormData(form)});
                if (!response.ok) throw new Error(`TCG action failed with status ${response.status}`);
            } catch (error) {
                window.showFrontendNotification?.({type: "ERROR", message: "The TCG operation could not be started."});
                console.error(error);
            } finally {
                submitButton?.removeAttribute("disabled");
            }
        });
    });

    function sortValue(cell) { return cell.dataset.sortValue ?? cell.textContent.trim(); }
    function compare(left, right, sortType) {
        const leftMissing = left === "" || left === null;
        const rightMissing = right === "" || right === null;
        if (leftMissing || rightMissing) return leftMissing === rightMissing ? 0 : leftMissing ? 1 : -1;
        if (sortType === "number") return Number(left) - Number(right);
        if (sortType === "date") return Date.parse(left) - Date.parse(right);
        return left.localeCompare(right, "hu", {numeric: true, sensitivity: "base"});
    }
})();
