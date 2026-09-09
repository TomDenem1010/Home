(() => {
    const player = document.getElementById("media-player");
    if (!player) return;
    const status = document.getElementById("media-playback-status");
    document.querySelectorAll(".media-video").forEach(button => {
        button.addEventListener("click", () => {
            document.querySelectorAll(".media-video").forEach(item => item.removeAttribute("aria-current"));
            button.setAttribute("aria-current", "true");
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

