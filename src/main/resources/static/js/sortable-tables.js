document.querySelectorAll("[data-sortable-table]").forEach((table) => {
    const headers = Array.from(table.querySelectorAll("thead th"));
    const body = table.tBodies[0];

    headers.forEach((header, columnIndex) => {
        const button = header.querySelector(".sort-button");
        if (!button) {
            return;
        }

        button.addEventListener("click", () => {
            const ascending = header.getAttribute("aria-sort") !== "ascending";
            const sortType = header.dataset.sortType || "text";
            const rows = Array.from(body.rows);

            rows.sort((leftRow, rightRow) => {
                const left = sortValue(leftRow.cells[columnIndex]);
                const right = sortValue(rightRow.cells[columnIndex]);
                const comparison = compare(left, right, sortType);
                return ascending ? comparison : -comparison;
            });

            headers.forEach((otherHeader) => otherHeader.removeAttribute("aria-sort"));
            header.setAttribute("aria-sort", ascending ? "ascending" : "descending");
            rows.forEach((row) => body.appendChild(row));
        });
    });
});

function sortValue(cell) {
    return cell.dataset.sortValue ?? cell.textContent.trim();
}

function compare(left, right, sortType) {
    const leftMissing = left === "" || left === null;
    const rightMissing = right === "" || right === null;
    if (leftMissing || rightMissing) {
        return leftMissing === rightMissing ? 0 : leftMissing ? 1 : -1;
    }

    if (sortType === "number") {
        return Number(left) - Number(right);
    }
    if (sortType === "date") {
        return Date.parse(left) - Date.parse(right);
    }
    return left.localeCompare(right, "hu", {numeric: true, sensitivity: "base"});
}
