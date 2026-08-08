document.addEventListener("DOMContentLoaded", async () => {
    const roleContainer = document.querySelector("#available-roles");
    const createForm = document.querySelector("#create-user-form");
    const authenticateForm = document.querySelector("#authenticate-form");

    const showResult = (element, message, success) => {
        element.textContent = message;
        element.classList.toggle("success", success);
        element.classList.toggle("error", !success);
    };

    const errorMessage = async response => {
        const body = await response.json().catch(() => ({}));
        return body.message || `Request failed (${response.status})`;
    };

    try {
        const response = await fetch("/auth/roles");
        if (!response.ok) throw new Error(await errorMessage(response));
        const roles = await response.json();
        roleContainer.replaceChildren(...roles.map(role => {
            const label = document.createElement("label");
            label.className = "role-option";
            const checkbox = document.createElement("input");
            checkbox.type = "checkbox";
            checkbox.name = "roles";
            checkbox.value = role;
            label.append(checkbox, document.createTextNode(role));
            return label;
        }));
    } catch (error) {
        roleContainer.textContent = error.message;
        roleContainer.classList.add("error");
    }

    createForm.addEventListener("submit", async event => {
        event.preventDefault();
        const data = new FormData(createForm);
        const result = document.querySelector("#create-user-result");
        const payload = {
            username: data.get("username"),
            password: data.get("password"),
            roles: data.getAll("roles")
        };

        const response = await fetch("/auth/users", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(payload)
        });
        if (!response.ok) {
            showResult(result, await errorMessage(response), false);
            return;
        }
        const user = await response.json();
        showResult(result, `${user.username} created with roles: ${user.roles.join(", ") || "none"}`, true);
        createForm.reset();
    });

    authenticateForm.addEventListener("submit", async event => {
        event.preventDefault();
        const data = new FormData(authenticateForm);
        const result = document.querySelector("#authenticate-result");
        const response = await fetch("/auth/authenticate", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({username: data.get("username"), password: data.get("password")})
        });
        if (!response.ok) {
            showResult(result, await errorMessage(response), false);
            return;
        }
        const roles = await response.json();
        showResult(result, roles.length ? `Authenticated: ${roles.join(", ")}` : "Invalid credentials", roles.length > 0);
    });
});
