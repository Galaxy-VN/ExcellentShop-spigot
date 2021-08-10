package su.nightexpress.nexshop.shop.virtual.editor.handler;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;

public interface IEditorHandler<T> {

    boolean onType(@NotNull Player player, @Nullable T obj, @NotNull VirtualEditorType type, @NotNull String msg);
}
