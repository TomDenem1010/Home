(() => {
    const logSearchForm = document.querySelector("#application-log-search-form");
    document.querySelector("[data-log-page-size]")?.addEventListener("change", () => logSearchForm?.requestSubmit());
    logSearchForm?.addEventListener("submit", event => {
        const sort = event.submitter?.dataset.logSort;
        if (sort) {
            const currentSort = logSearchForm.elements.namedItem("sort");
            const direction = logSearchForm.elements.namedItem("direction");
            direction.value = currentSort.value === sort && direction.value === "DESC" ? "ASC" : "DESC";
            currentSort.value = sort;
        }
        logSearchForm.elements.namedItem("page").value = event.submitter?.dataset.logPage ?? "0";
    });
    const userSelect = document.querySelector("#role-user-select");
    if (!userSelect) return;

    const roleCheckboxes = [...document.querySelectorAll(".role-checkbox")];
    userSelect.addEventListener("change", () => {
        const selectedRoles = new Set(
            (userSelect.selectedOptions[0]?.dataset.roles ?? "").split(",").filter(Boolean)
        );
        roleCheckboxes.forEach(checkbox => {
            checkbox.checked = selectedRoles.has(checkbox.value);
        });
    });
})();
