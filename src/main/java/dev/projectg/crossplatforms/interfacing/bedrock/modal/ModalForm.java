package dev.projectg.crossplatforms.interfacing.bedrock.modal;

import dev.projectg.crossplatforms.CrossplatForms;
import dev.projectg.crossplatforms.Logger;
import dev.projectg.crossplatforms.handler.BedrockHandler;
import dev.projectg.crossplatforms.interfacing.ClickAction;
import dev.projectg.crossplatforms.interfacing.InterfaceRegistry;
import dev.projectg.crossplatforms.interfacing.bedrock.BedrockForm;
import dev.projectg.crossplatforms.utils.PlaceholderUtils;
import lombok.ToString;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.response.ModalFormResponse;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Required;

import java.util.UUID;

@ToString
@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class ModalForm extends BedrockForm {

    private String content = "";

    @Required
    private ModalButton button1;

    @Required
    private ModalButton button2;

    @Override
    public void send(@NotNull dev.projectg.crossplatforms.handler.Player recipient) {
        InterfaceRegistry registry = CrossplatForms.getInstance().getInterfaceRegistry();
        Logger logger = Logger.getLogger();
        UUID uuid = recipient.getUuid();
        Player player = (Player) recipient;

        BedrockHandler bedrockHandler = CrossplatForms.getInstance().getBedrockHandler();
        if (!bedrockHandler.isBedrockPlayer(uuid)) {
            logger.severe("Player with UUID " + uuid + " is not a Bedrock Player!");
            return;
        }

        org.geysermc.cumulus.ModalForm form = org.geysermc.cumulus.ModalForm.of(
                PlaceholderUtils.setPlaceholders(player, super.getTitle()),
                PlaceholderUtils.setPlaceholders(player, content),
                PlaceholderUtils.setPlaceholders(player, button1.getText()),
                PlaceholderUtils.setPlaceholders(player, button2.getText()));

        // Set the response handler
        form.setResponseHandler((responseData) -> {
            ModalFormResponse response = form.parseResponse(responseData);
            if (!response.isCorrect()) {
                // isCorrect() = !isClosed() && !isInvalid()
                // player closed the form or returned invalid info (see FormResponse)
                return;
            }

            int id = response.getClickedButtonId();
            ClickAction action = switch (id) {
                case 0 -> button1;
                case 1 -> button2;
                default -> throw new AssertionError();
            };

            // Handle effects of pressing the button
            action.affectPlayer(registry, player);
        });

        // Send the form to the floodgate player
        bedrockHandler.sendForm(uuid, form);
    }
}
