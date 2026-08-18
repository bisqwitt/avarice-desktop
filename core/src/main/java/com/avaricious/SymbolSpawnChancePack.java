package com.avaricious;

import com.avaricious.components.buttons.Button;
import com.avaricious.components.popups.ClaimedPopup;
import com.avaricious.components.popups.PopupManager;
import com.avaricious.components.popups.SoldPopup;
import com.avaricious.components.slot.SlotMachine;
import com.avaricious.components.slot.Symbol;
import com.avaricious.items.upgrades.AbstractUpgrade;
import com.avaricious.items.upgrades.IUpgradeType;
import com.avaricious.items.upgrades.UpgradeRarity;
import com.avaricious.items.upgrades.cards.CardType;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.SeededRandomizer;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;

public class SymbolSpawnChancePack extends PackOpening {

    private Symbol result;

    public SymbolSpawnChancePack() {
        super(new Rectangle(5.65f, 9.25f, 1.7f, 1.7f));
    }

    @Override
    protected TextureRegion getTexture() {
        if (ripped) return Assets.I().get(result.textureKey());
        return Assets.I().get(Symbol.values()[currentTextureIndex].textureKey());
    }

    @Override
    protected TextureRegion getShadowTexture() {
        if (ripped) return Assets.I().get(result.shadowKey());
        return Assets.I().get(Symbol.values()[currentTextureIndex].shadowKey());
    }

    @Override
    protected TextureRegion getWhiteTexture() {
        if (ripped) return Assets.I().get(result.whiteKey());
        return Assets.I().get(Symbol.values()[currentTextureIndex].whiteKey());
    }

    @Override
    protected int getTextureAmount() {
        return Symbol.values().length;
    }

    @Override
    protected AbstractUpgrade getPackDescription() {
        return new AbstractUpgrade() {
            @Override
            public String title() {
                return "Symbol";
            }

            @Override
            public String description() {
                return "Increase Spawn chance of random Symbol by 5%\n"
                    + "(Can be discarded)";
            }

            @Override
            public IUpgradeType type() {
                return CardType.UNKNOWN;
            }

            @Override
            public int price() {
                return 5;
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
                return 2.25f;
            }
        };
    }

    @Override
    protected AbstractUpgrade getResult() {
        result = Symbol.values()[SeededRandomizer.nextInt(0, Symbol.values().length - 1)];
        return new AbstractUpgrade() {
            @Override
            public String title() {
                return "Spawn chance";
            }

            @Override
            public String description() {
                return "Increase " + result.toString() + " spawn chance from " + result.poolCount()
                    + "% to " + (result.poolCount() + 5) + "%";
            }

            @Override
            public IUpgradeType type() {
                return CardType.UNKNOWN;
            }

            @Override
            public int price() {
                return 5;
            }

            @Override
            public TextureRegion texture() {
                return Assets.I().get(result.textureKey());
            }

            @Override
            public TextureRegion shadowTexture() {
                return Assets.I().get(result.shadowKey());
            }

            @Override
            public float getTextureHeight() {
                return 0;
            }

            @Override
            public float getTextureWidth() {
                return 0;
            }

            @Override
            public float getTooltipYOffset() {
                return 2.25f;
            }
        };
    }

    @Override
    protected Button getSellButton() {
        return new Button(() -> {
            PopupManager.I().killTooltip(tooltipPopup);
            closing = true;
            PopupManager.I().spawnTextPopup(new SoldPopup(new Vector2(2.5f, 11f), ZIndex.PACK_OPENING_SELECTED));
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    close();
                }
            }, 1);
        },
            Assets.I().get(AssetKey.DISCARD_BUTTON),
            Assets.I().get(AssetKey.DISCARD_BUTTON_PRESSED),
            Assets.I().get(AssetKey.DISCARD_BUTTON),
            new Rectangle(1, 5, 79 / 25f, 25 / 25f),
            Input.Keys.BACKSPACE, ZIndex.PACK_OPENING_SELECTED);
    }

    @Override
    protected Button getClaimButton() {
        return new Button(() -> {
            Symbol.increaseSpawnChance(result, 5);
            SlotMachine.I().buildStrip();
            PopupManager.I().killTooltip(tooltipPopup);
            closing = true;
            PopupManager.I().spawnTextPopup(new ClaimedPopup(new Vector2(2.5f, 11f), ZIndex.PACK_OPENING_SELECTED));
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
}
