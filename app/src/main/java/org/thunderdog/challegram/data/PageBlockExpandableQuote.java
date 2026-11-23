/* SPDX-License-Identifier: GPL-3.0-or-later */
package org.thunderdog.challegram.data;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import org.drinkless.tdlib.TdApi;
import org.thunderdog.challegram.R;
import org.thunderdog.challegram.core.Lang;
import org.thunderdog.challegram.loader.ComplexReceiver;
import org.thunderdog.challegram.loader.Receiver;
import org.thunderdog.challegram.navigation.ViewController;
import org.thunderdog.challegram.telegram.TdlibUi;
import org.thunderdog.challegram.theme.Theme;
import org.thunderdog.challegram.tool.Screen;
import org.thunderdog.challegram.widget.ArticleBodyView;
import tgx.td.Td;

public final class PageBlockExpandableQuote extends PageBlockRichText {
  private boolean expanded;
  private int fullHeight, visibleHeight;
  private final Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
  public PageBlockExpandableQuote (ViewController<?> context, TdApi.PageBlockExpandableBlockQuote quote, int quoteLevel, TdlibUi.UrlOpenParameters parameters) {
    super(
      context,
      quote,
      withCredit(joinParagraphs(quote.blocks), quote.credit),
      quoteLevel + 1,
      16f,
      false,
      parameters
    );
    label.setTextSize(Screen.dp(14f));
  }
  // The quote comes as blocks, but is drawn as one text: only paragraphs are shown
  private static TdApi.RichText joinParagraphs (TdApi.PageBlock[] blocks) {
    java.util.ArrayList<TdApi.RichText> parts = new java.util.ArrayList<>();
    if (blocks != null) {
      for (TdApi.PageBlock block : blocks) {
        if (block instanceof TdApi.PageBlockParagraph && !parts.isEmpty()) {
          parts.add(new TdApi.RichTextPlain("\n"));
        }
        if (block instanceof TdApi.PageBlockParagraph) {
          parts.add(((TdApi.PageBlockParagraph) block).text);
        }
      }
    }
    if (parts.isEmpty()) {
      return new TdApi.RichTextPlain("");
    }
    if (parts.size() == 1) {
      return parts.get(0);
    }
    return new TdApi.RichTexts(parts.toArray(new TdApi.RichText[0]));
  }
  private static TdApi.RichText withCredit (TdApi.RichText text, TdApi.RichText credit) {
    if (Td.isEmpty(credit)) {
      return text;
    }
    return new TdApi.RichTexts(new TdApi.RichText[] {
      text,
      new TdApi.RichTextPlain("\n"),
      new TdApi.RichTextItalic(credit)
    });
  }
  @Override protected int computeHeight (View view, int width) {
    fullHeight = super.computeHeight(view, width);
    visibleHeight = expanded ? fullHeight : Math.min(fullHeight, Screen.dp(120f));
    return visibleHeight + (fullHeight > Screen.dp(120f) ? Screen.dp(40f) : 0);
  }
  @Override public boolean isClickable () { return true; }
  @Override public boolean handleTouchEvent (View view, MotionEvent event) { return event.getY() < visibleHeight && super.handleTouchEvent(view, event); }
  @Override public boolean onClick (View view, boolean longPress) {
    if (longPress || fullHeight <= Screen.dp(120f)) return false;
    expanded = !expanded; invalidateHeight(view); currentViews.invalidate();
    ArticleBodyView body = ArticleBodyView.find(view);
    if (body != null) body.relayoutArticle();
    return true;
  }
  @Override public void drawInternal (View view, Canvas canvas, Receiver preview, Receiver receiver, ComplexReceiver icons) {
    int save = canvas.save(); canvas.clipRect(0, 0, view.getWidth(), visibleHeight);
    super.drawInternal(view, canvas, preview, receiver, icons); canvas.restoreToCount(save);
    if (fullHeight > Screen.dp(120f)) {
      label.setColor(Theme.textLinkColor());
      canvas.drawText(Lang.getString(expanded ? R.string.ArticleShowLess : R.string.ArticleReadMore), Screen.dp(28f), visibleHeight + Screen.dp(25f), label);
    }
  }
}
