package dev.projectg.crossplatforms.interfacing.bedrock.custom;

import com.google.gson.JsonPrimitive;
import dev.projectg.crossplatforms.CrossplatForms;
import dev.projectg.crossplatforms.Logger;
import dev.projectg.crossplatforms.handler.BedrockHandler;
import dev.projectg.crossplatforms.interfacing.BasicClickAction;
import dev.projectg.crossplatforms.interfacing.InterfaceManager;
import dev.projectg.crossplatforms.interfacing.bedrock.BedrockForm;
import dev.projectg.crossplatforms.utils.PlaceholderUtils;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.response.CustomFormResponse;
import org.geysermc.cumulus.util.FormImage;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Required;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ConfigSerializable
@SuppressWarnings("FieldMayBeFinal")
public class CustomForm extends BedrockForm {

    private FormImage image = null;
    private List<CustomComponent> components = Collections.emptyList();

    @Required
    private BasicClickAction action = null;

    @Override
    public void send(@NotNull dev.projectg.crossplatforms.handler.Player recipient) {
        InterfaceManager registry = CrossplatForms.getInstance().getInterfaceManager();
        Logger logger = Logger.getLogger();
        UUID uuid = recipient.getUuid();
        Player player = (Player) recipient.getHandle();

        BedrockHandler bedrockHandler = CrossplatForms.getInstance().getBedrockHandler();
        if (!bedrockHandler.isBedrockPlayer(uuid)) {
            logger.severe("Player with UUID " + uuid + " is not a Bedrock Player!");
            return;
        }

        List<CustomComponent> components = new ArrayList<>();
        for (CustomComponent component : this.components) {
            components.add(component.withPlaceholders((text) -> PlaceholderUtils.setPlaceholders(player, text)));
        }

        @SuppressWarnings("unchecked")
        org.geysermc.cumulus.CustomForm customForm = org.geysermc.cumulus.CustomForm.of(
                PlaceholderUtils.setPlaceholders(player, super.getTitle()),
                image,
                (List<org.geysermc.cumulus.component.Component>)(List<?>) components // sad noises
        );

        // Set the response handler
        customForm.setResponseHandler((responseData) -> {
            CustomFormResponse response = customForm.parseResponse(responseData);
            if (!response.isCorrect()) {
                // isCorrect() = !isClosed() && !isInvalid()
                // player closed the form or returned invalid info (see FormResponse)
                return;
            }

            Map<String, String> resultPlaceholders = new HashMap<>();
            for (int i = 0; i < components.size(); i++) {

                JsonPrimitive primitive = response.get(i);
                if (primitive == null) {
                    logger.severe("Failed to get response " + i + " from custom form " + super.getTitle());
                    logger.severe("Full response data:");
                    logger.severe(responseData);
                    return;
                }

                resultPlaceholders.put("%result_" + i + "%", primitive.getAsString());
            }

            if (logger.isDebug()) {
                logger.info("Placeholder results for CustomForm " + getTitle());
                for (Map.Entry<String, String> entry : resultPlaceholders.entrySet()) {
                    logger.info(entry.getKey() + ": " + entry.getValue());
                }
            }

            // Handle effects of pressing the button
            action.affectPlayer(player, resultPlaceholders, registry, bedrockHandler);
        });

        // Send the form to the floodgate player
        bedrockHandler.sendForm(uuid, customForm);
    }
}
