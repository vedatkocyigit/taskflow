const COLORS = [
    "#E3F2FD",
    "#E8F5E9",
    "#FFF3E0",
    "#FCE4EC",
    "#EDE7F6",
    "#F3E5F5",
    "#E0F2F1",
];

export function getTagColor(tag: string) {
    let hash = 0;
    for (let i = 0; i < tag.length; i++) {
        hash = tag.charCodeAt(i) + ((hash << 5) - hash);
    }
    return COLORS[Math.abs(hash) % COLORS.length];
}
