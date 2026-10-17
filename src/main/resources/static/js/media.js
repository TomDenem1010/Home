(() => {
    const player = document.getElementById("media-player");
    if (!player) return;
    player.volume = 0.5;
    const status = document.getElementById("media-playback-status");
    const tagPanel = document.getElementById("media-tags");
    const tagList = document.getElementById("media-tag-list");
    const tagStatus = document.getElementById("media-tag-status");
    let tagRequest = 0;

    async function loadTags(url) {
        const request = ++tagRequest;
        tagPanel.hidden = false;
        tagList.replaceChildren();
        tagStatus.textContent = "Loading tags...";
        try {
            const response = await fetch(url);
            if (!response.ok) throw new Error("Unable to load tags");
            const tags = await response.json();
            if (request !== tagRequest) return;
            tagStatus.textContent = tags.length ? "" : "No tags yet. Create tags in Video operations.";
            tags.forEach(tag => {
                const button = document.createElement("button");
                button.type = "button";
                button.className = "media-tag";
                button.textContent = tag.name;
                button.setAttribute("aria-pressed", String(tag.assigned));
                button.addEventListener("click", async () => {
                    const assigned = button.getAttribute("aria-pressed") !== "true";
                    button.disabled = true;
                    try {
                        const csrf = document.getElementById("media-tag-csrf");
                        const result = await fetch(`${url}/${encodeURIComponent(tag.id)}`, {
                            method: "POST",
                            headers: {[csrf.dataset.header]: csrf.value},
                            body: new URLSearchParams({assigned: String(assigned)})
                        });
                        if (result.status !== 200 || result.redirected) throw new Error("Unable to save tag");
                        if (request === tagRequest) {
                            button.setAttribute("aria-pressed", String(assigned));
                            tagStatus.textContent = "";
                        }
                    } catch {
                        if (request === tagRequest) tagStatus.textContent = "Could not save the tag. Please try again.";
                    } finally {
                        button.disabled = false;
                    }
                });
                tagList.append(button);
            });
        } catch {
            if (request === tagRequest) tagStatus.textContent = "Could not load tags. Select the video again to retry.";
        }
    }
    document.querySelectorAll(".media-video").forEach(button => {
        button.addEventListener("click", () => {
            document.querySelectorAll(".media-video").forEach(item => item.removeAttribute("aria-current"));
            button.setAttribute("aria-current", "true");
            loadTags(button.dataset.tags);
            document.getElementById("media-playing").textContent = button.dataset.name;
            status.textContent = "Loading video...";
            player.src = button.dataset.stream;
            player.load();
            player.play().catch(() => { status.textContent = "Press play to start, or check whether your browser supports this video format."; });
        });
    });
    player.addEventListener("playing", () => { status.textContent = ""; });
    player.addEventListener("error", () => { status.textContent = "Video unavailable or its format is not supported by your browser."; });
})();
