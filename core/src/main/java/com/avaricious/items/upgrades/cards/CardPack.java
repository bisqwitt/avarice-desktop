package com.avaricious.items.upgrades.cards;

import com.avaricious.CreditManager;
import com.avaricious.PackOpening;
import com.avaricious.components.buttons.Button;
import com.avaricious.components.popups.ClaimedPopup;
import com.avaricious.components.popups.PopupManager;
import com.avaricious.components.popups.SoldPopup;
import com.avaricious.items.upgrades.AbstractUpgrade;
import com.avaricious.items.upgrades.Deck;
import com.avaricious.items.upgrades.IUpgradeType;
import com.avaricious.items.upgrades.UpgradeRarity;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;

import java.util.Arrays;
import java.util.List;

public class CardPack extends PackOpening {

    private AbstractCard resultCard;

    public CardPack() {
        super(new Rectangle(1.5f, 8.1f, AbstractCard.WIDTH / 80f, AbstractCard.HEIGHT / 80f));
    }

    @Override
    protected TextureRegion getTexture() {
        if (ripped) return resultCard.texture();
        return Assets.I().get(cardAssetKeys().get(currentTextureIndex));
    }

    @Override
    protected TextureRegion getShadowTexture() {
        return Assets.I().get(AssetKey.JOKER_CARD_SHADOW);
    }

    @Override
    protected TextureRegion getWhiteTexture() {
        return Assets.I().get(AssetKey.WHITE_JOKER_CARD);
    }

    @Override
    protected int getTextureAmount() {
        return cardAssetKeys().size();
    }

    @Override
    protected AbstractUpgrade getPackDescription() {
        return new AbstractUpgrade() {
            @Override
            public String title() {
                return "Card";
            }

            @Override
            public String description() {
                return "Random Card";
            }

            @Override
            public IUpgradeType type() {
                return CardType.UNKNOWN;
            }

            @Override
            public int price() {
                return 1;
            }

            @Override
            public TextureRegion texture() {
                return null;
            }

            @Override
            public TextureRegion shadowTexture() {
                return null;
            }

            @Override
            public float getTextureWidth() {
                return 0;
            }

            @Override
            public float getTextureHeight() {
                return 0;
            }

            @Override
            public UpgradeRarity rarity() {
                return UpgradeRarity.UNKNOWN;
            }

            @Override
            public float getTooltipYOffset() {
                return 3f;
            }
        };
    }

    @Override
    protected AbstractUpgrade getResult() {
        return resultCard = AbstractCard.randomCard();
    }

    @Override
    protected Button getSellButton() {
        return new Button(() -> {
            CreditManager.I().gain(resultCard.price());
            PopupManager.I().killTooltip(tooltipPopup);
            closing = true;
            PopupManager.I().spawnTextPopup(new SoldPopup(new Vector2(2.5f, 12f), ZIndex.PACK_OPENING_SELECTED));
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    close();
                }
            }, 1);
        },
            Assets.I().get(AssetKey.SELL_BUTTON),
            Assets.I().get(AssetKey.SELL_BUTTON_PRESSED),
            Assets.I().get(AssetKey.SELL_BUTTON),
            new Rectangle(1, 5, 79 / 25f, 25 / 25f),
            Input.Keys.BACKSPACE, ZIndex.PACK_OPENING_SELECTED);
    }

    @Override
    protected Button getClaimButton() {
        return new Button(() -> {
            Deck.I().addCardToDeck(resultCard);
            PopupManager.I().killTooltip(tooltipPopup);
            closing = true;
            PopupManager.I().spawnTextPopup(new ClaimedPopup(new Vector2(2.5f, 12f), ZIndex.PACK_OPENING_SELECTED));
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    close();
                }
            }, 1);
        },
            Assets.I().get(AssetKey.CLAIM_BUTTON),
            Assets.I().get(AssetKey.CLAIM_BUTTON_PRESSED),
            Assets.I().get(AssetKey.CLAIM_BUTTON),
            new Rectangle(4.9f, 5f, 79 / 25f, 25 / 25f),
            Input.Keys.ENTER, ZIndex.PACK_OPENING_SELECTED);
    }

    private List<AssetKey> cardAssetKeys() {
        return Arrays.asList(
            AssetKey.BALL8_CARD,
            AssetKey.BANNER_CARD,
            AssetKey.BLACKBOARD_CARD,
            AssetKey.CEREMONIAL_DAGGER_CARD,
            AssetKey.CREDIT_CARD_CARD,
            AssetKey.DELAYED_GRATIFICATION_CARD,
            AssetKey.DNA_CARD,
            AssetKey.DUSK_CARD,
            AssetKey.MIME_CARD,
            AssetKey.MISPRINT_CARD,
            AssetKey.MYSTIC_SUMMIT_CARD,
            AssetKey.SPACE_JOKER_CARD,
            AssetKey.SUPERNOVA_CARD
        );
    }

}
