document.addEventListener("DOMContentLoaded", () => {
    const userSelect = document.querySelector("#role-user-select");
    if (!userSelect) {
        return;
    }

    const roleCheckboxes = [...document.querySelectorAll(".role-checkbox")];
    userSelect.addEventListener("change", () => {
        const selectedRoles = new Set(
            (userSelect.selectedOptions[0]?.dataset.roles ?? "")
                .split(",")
                .filter(Boolean)
        );
        roleCheckboxes.forEach(checkbox => {
            checkbox.checked = selectedRoles.has(checkbox.value);
        });
    });
});
