package org.thunderdog.challegram.component.popups;

import android.content.Context;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.drinkless.tdlib.TdApi;
import org.thunderdog.challegram.R;
import org.thunderdog.challegram.U;
import org.thunderdog.challegram.component.attach.MediaBottomBaseController;
import org.thunderdog.challegram.component.attach.MediaLayout;
import org.thunderdog.challegram.component.base.SettingView;
import org.thunderdog.challegram.core.Lang;
import org.thunderdog.challegram.data.TGMessageServiceImpl;
import org.thunderdog.challegram.navigation.BackHeaderButton;
import org.thunderdog.challegram.navigation.HeaderView;
import org.thunderdog.challegram.support.ViewSupport;
import org.thunderdog.challegram.telegram.TdlibUi;
import org.thunderdog.challegram.theme.ColorId;
import org.thunderdog.challegram.theme.Theme;
import org.thunderdog.challegram.tool.Screen;
import org.thunderdog.challegram.ui.ListItem;
import org.thunderdog.challegram.ui.PollResultsController;
import org.thunderdog.challegram.ui.SettingHolder;
import org.thunderdog.challegram.ui.SettingsAdapter;
import org.thunderdog.challegram.ui.ShareController;
import org.thunderdog.challegram.util.AvatarDrawModifier;
import org.thunderdog.challegram.util.text.FormattedText;
import org.thunderdog.challegram.util.text.Text;
import org.thunderdog.challegram.util.text.TextColorSets;
import org.thunderdog.challegram.util.text.TextEntity;
import org.thunderdog.challegram.util.text.TextEntityCustom;
import org.thunderdog.challegram.widget.CustomTextView;
import org.thunderdog.challegram.widget.GiftHeaderView;
import org.thunderdog.challegram.widget.ScalableTextView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import me.vkryl.android.ViewUtils;
import me.vkryl.android.widget.FrameLayoutFix;
import me.vkryl.td.ChatId;
import me.vkryl.td.MessageId;
import me.vkryl.td.Td;

public class GiftCodeController extends MediaBottomBaseController<Void> implements View.OnClickListener {
  private final TdApi.PremiumGiftCodeInfo info;
  private final String code;

  private int measuredRecyclerHeight;

  protected GiftCodeController (MediaLayout context, String code, TdApi.PremiumGiftCodeInfo giftCodeInfo) {
    super(context, Lang.getString(R.string.GiftLink));
    this.info = giftCodeInfo;
    this.code = code;
  }

  @Override
  protected View onCreateView (Context context) {
    buildContentView(false);
    setLayoutManager(new LinearLayoutManager(context(), RecyclerView.VERTICAL, false));

    final boolean isActivated = info.useDate > 0;

    ArrayList<ListItem> items = new ArrayList<>();

    items.add(new ListItem(ListItem.TYPE_GIFT_HEADER));
    items.add(new ListItem(ListItem.TYPE_SETTING, R.id.btn_copyLink, R.drawable.baseline_link_24, R.string.GiftLink)
      .setStringValue(getGiftCodeLink()));

    if (info.creatorId != null) {
      items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
      items.add(new ListItem(ListItem.TYPE_SETTING, R.id.btn_openGiftSender, 0, R.string.GiftFrom)
        .setStringValue(tdlib.chatTitle(Td.getSenderId(info.creatorId)))
        .setDrawModifier(new AvatarDrawModifier(24))
        .setData(info.creatorId)
      );
    }
    if (info.userId != 0) {
      items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
      items.add(new ListItem(ListItem.TYPE_SETTING, R.id.btn_openGiftReceiver, 0, R.string.GiftTo)
        .setStringValue(tdlib.chatTitle(ChatId.fromUserId(info.userId), false, false))
        .setDrawModifier(new AvatarDrawModifier(24))
        .setData(new TdApi.MessageSenderUser(info.userId))
      );
    }
    items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
    items.add(new ListItem(ListItem.TYPE_SETTING,0, R.drawable.dotvhs_baseline_gift_24, R.string.Gift)
      .setStringValue(Lang.plural(R.string.xTelegramPremiumForMonth, info.monthCount)));
    items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
    items.add(new ListItem(ListItem.TYPE_SETTING, R.id.btn_showGiveawayMessage, R.drawable.baseline_info_24, R.string.GiftReason)
      .setStringValue(Lang.getString(info.isFromGiveaway ? R.string.GiftReasonGiveaway : R.string.GiftReasonGift)));
    items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
    items.add(new ListItem(ListItem.TYPE_SETTING,0, R.drawable.baseline_date_range_24, R.string.GiftDate)
      .setStringValue(Lang.dateFullShort(info.creationDate, TimeUnit.SECONDS)));

    items.add(new ListItem(ListItem.TYPE_SHADOW_BOTTOM));

    if (isActivated) {
      items.add(new ListItem(ListItem.TYPE_DESCRIPTION, 0, 0, Lang.getString(R.string.GiftLinkWasActivated, Lang.dateFullShort(info.useDate, TimeUnit.SECONDS), Lang.time(info.useDate, TimeUnit.SECONDS))));
    } else {
      items.add(new ListItem(ListItem.TYPE_TEXT_VIEW));
      items.add(new ListItem(ListItem.TYPE_SHADOW_TOP));
      items.add(new ListItem(ListItem.TYPE_BUTTON, R.id.btn_redeemGiftLink, 0, R.string.GiftLinkRedeem));
    }

    FrameLayoutFix.LayoutParams params;
    params = FrameLayoutFix.newParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.topMargin = HeaderView.getSize(false);
    params.bottomMargin = HeaderView.getTopOffset();
    recyclerView.setLayoutParams(params);

    SettingsAdapter adapter = new SettingsAdapter(this) {
      @Override
      public void updateView (SettingHolder holder, int position, int viewType) {
        super.updateView(holder, position, viewType);
        if (viewType == ListItem.TYPE_GIFT_HEADER) {
          final boolean isActivated = info.useDate > 0;
          GiftHeaderView view = (GiftHeaderView) (holder.itemView);
          view.setTexts(
            isActivated ? R.string.GiftLinkActivated : R.string.GiftLink,
            isActivated ? R.string.GiftLinkActivatedDesc: R.string.GiftLinkDesc);
        }
      }

      @Override
      protected void setValuedSetting (ListItem item, SettingView view, boolean isUpdate) {
        view.setType(SettingView.TYPE_INFO_COMPACT);
        view.setData(item.getStringValue());
        view.forcePadding(Screen.dp(72), Screen.dp(16));
        AvatarDrawModifier dm = (AvatarDrawModifier) item.getDrawModifier();
        if (dm != null) {
          view.setDrawModifier(dm);
          dm.requestFiles(view.getComplexReceiver(), tdlib, (TdApi.MessageSender) item.getData());
        }
      }

      @Override
      protected void setText (ListItem item, CustomTextView view, boolean isUpdate) {

        String gvw = Lang.getString(R.string.GiftLinkShareDesc_SendThisLink);
        TextEntityCustom custom = new TextEntityCustom(GiftCodeController.this, tdlib, gvw, 0, gvw.length(), 0, null);
        custom.setCustomColorSet(TextColorSets.Regular.LINK);
        custom.setOnClickListener(new ClickableSpan() {
          @Override
          public void onClick (@NonNull View widget) {
            tdlib.ui().shareText(GiftCodeController.this, getGiftCodeLink());
          }
        });
        FormattedText gvwf = new FormattedText(gvw, new TextEntity[] {custom});

        FormattedText formattedText = TGMessageServiceImpl.getText(tdlib, null, R.string.GiftLinkShareDesc, gvwf);

        view.setText(formattedText.text, formattedText.entities, false);
        view.setTextSize(15f);
        view.setPadding(Screen.dp(16f), Screen.dp(7f), Screen.dp(16f), Screen.dp(12f));
        view.setTextColorId(ColorId.background_text);
      }

      @Override
      protected void setButtonText (ListItem item, ScalableTextView view, boolean isUpdate) {
        super.setButtonText(item, view, isUpdate);

        view.setTextColor(Theme.getColor(ColorId.badgeText));
        addThemeTextColorListener(view, ColorId.badgeText);

        ViewUtils.setBackground(view, Theme.fillingSelector(ColorId.badge));
        addThemeInvalidateListener(view);
      }
    };
    adapter.setItems(items.toArray(new ListItem[0]), false);
    initMetrics();

    ViewSupport.setThemedBackground(recyclerView, ColorId.background);
    recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
      @Override
      public void onLayoutChange (View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        recyclerView.removeOnLayoutChangeListener(this);
        measuredRecyclerHeight = recyclerView.getMeasuredHeight();
        initMetrics();
      }
    });

    setAdapter(adapter);

    return contentView;
  }

  @Override
  protected int getInitialContentHeight () {
    if (measuredRecyclerHeight != 0) {
      return measuredRecyclerHeight;
    }

    return super.getInitialContentHeight();
  }

  @Override
  public boolean ignoreStartHeightLimits () {
    return true;
  }

  @Override
  protected boolean canExpandHeight () {
    return false;
  }

  @Override
  protected int getBackButton () {
    return BackHeaderButton.TYPE_CLOSE;
  }

  @Override
  public boolean onBackPressed (boolean fromTop) {
    mediaLayout.hide(false);
    return true;
  }

  @Override
  public int getId () {
    return R.id.controller_giftDialog;
  }

  private boolean redeemLoading;

  @Override
  public void onClick (View v) {
    final int id = v.getId();
    if (id == R.id.btn_copyLink) {
      U.copyText(getGiftCodeLink());
    } else if (id == R.id.btn_openGiftSender) {
      if (info.creatorId != null) {
        tdlib.ui().openChat(this, info.creatorId, null);
        mediaLayout.hide(false);
      }
    } else if (id == R.id.btn_openGiftReceiver) {
      tdlib.ui().openChatProfile(this, ChatId.fromUserId(info.userId), null, null);
      mediaLayout.hide(false);
    } else if (id == R.id.btn_showGiveawayMessage) {
      final long chatId = Td.getSenderId(info.creatorId);
      tdlib.ui().openMessage(this, chatId, new MessageId(chatId, info.giveawayMessageId, null), null);
      mediaLayout.hide(false);
    } else if (id == R.id.btn_redeemGiftLink) {
      if (!redeemLoading) {
        redeemLoading = true;
       /* tdlib.client().send(new TdApi.ApplyPremiumGiftCode(code), tdlib.okHandler(() -> {
          mediaLayout.hide(false);
        }));*/
      }
    }
  }

  @Override
  protected ViewGroup createCustomBottomBar () {
    return new FrameLayout(context);
  }

  public final String getGiftCodeLink () {
    return "t.me/giftcode/" + code;
  }
}