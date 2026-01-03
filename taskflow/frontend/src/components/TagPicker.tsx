import { useEffect, useState } from "react";
import { listTags, createTag, attachTag, detachTag } from "../api/tag.api";
import type { Tag } from "../api/tag.api";

type Props = {
    workspaceId: number;
    projectId: number;
    taskId: number;
    value: string[]; // task üzerindeki tag name'ler
    onChange: (next: string[]) => void;
};

export default function TagPicker({
    workspaceId,
    projectId,
    taskId,
    value,
    onChange,
}: Props) {
    const [allTags, setAllTags] = useState<Tag[]>([]);
    const [input, setInput] = useState("");

    // =========================
    // LOAD TAGS
    // =========================
    useEffect(() => {
        const load = async () => {
            const tags = await listTags(workspaceId);
            setAllTags(tags);
        };
        load();
    }, [workspaceId]);

    // =========================
    // ADD TAG
    // =========================
    const addTag = async (name: string) => {
        const normalized = name.trim();
        if (!normalized || value.includes(normalized)) return;

        let tag = allTags.find(
            (t) => t.name.toLowerCase() === normalized.toLowerCase()
        );

        // yoksa backend'de oluştur
        if (!tag) {
            tag = await createTag(workspaceId, normalized);
            setAllTags((prev) => [...prev, tag!]);
        }

        await attachTag(projectId, taskId, tag.id);

        onChange([...value, tag.name]);
        setInput("");
    };

    // =========================
    // REMOVE TAG
    // =========================
    const removeTag = async (name: string) => {
        const tag = allTags.find((t) => t.name === name);
        if (!tag) return;

        await detachTag(projectId, taskId, tag.id);
        onChange(value.filter((t) => t !== name));
    };

    const suggestions = allTags.filter(
        (t) =>
            t.name.toLowerCase().includes(input.toLowerCase()) &&
            !value.includes(t.name)
    );

    return (
        <div className="tag-picker">
            {/* SELECTED TAGS */}
            <div className="tag-list">
                {value.map((t) => (
                    <span key={t} className="tag-pill">
                        {t}
                        <button onClick={() => removeTag(t)}>×</button>
                    </span>
                ))}
            </div>

            {/* INPUT */}
            <input
                className="tag-input"
                placeholder="Add tag…"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={(e) => {
                    if (e.key === "Enter" && input.trim()) {
                        addTag(input);
                    }
                }}
            />

            {/* AUTOCOMPLETE */}
            {input && suggestions.length > 0 && (
                <div className="tag-suggestions">
                    {suggestions.map((t) => (
                        <div
                            key={t.id}
                            className="tag-suggestion"
                            onClick={() => addTag(t.name)}
                        >
                            {t.name}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
