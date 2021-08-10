package su.nightexpress.nexshop.shop.chest.editor.handler;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.api.editor.IEditorHandler;
import su.nightexpress.nexshop.shop.chest.editor.ChestEditorType;

public class EditorHandlerShop implements IEditorHandler<IShopChest, ChestEditorType> {

    public EditorHandlerShop() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean onType(
            @NotNull Player player, @Nullable IShopChest shop,
            @NotNull ChestEditorType type, @NotNull String msg) {

        if (shop == null) {
            return true;
        }

        switch (type) {
            case CHANGE_NAME: {
                shop.setName(msg);
                shop.updateDisplayText();
                break;
            }
            default: {
                break;
            }
        }

        shop.save();
        shop.getEditor().open(player, 1);
        return true;
    }

}
