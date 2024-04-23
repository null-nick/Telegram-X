package org.thunderdog.challegram.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.NonNull;

import org.drinkless.tdlib.TdApi;
import org.thunderdog.challegram.data.PageBlockRichText;
import org.thunderdog.challegram.data.TD;
import org.thunderdog.challegram.navigation.ViewController;
import org.thunderdog.challegram.telegram.Tdlib;
import org.thunderdog.challegram.tool.Paints;
import org.thunderdog.challegram.tool.Screen;
import org.thunderdog.challegram.tool.UI;
import org.thunderdog.challegram.util.text.FormattedText;
import org.thunderdog.challegram.util.text.Text;
import org.thunderdog.challegram.util.text.TextColorSets;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class TextPerformanceLayoutController extends ViewController<TdApi.WebPageInstantView> {
  private FormattedText[] formattedTexts;

  public TextPerformanceLayoutController (@NonNull Context context, Tdlib tdlib) {
    super(context, tdlib);
  }

  @Override
  protected View onCreateView (Context context) {
    ArrayList<TdApi.RichText> texts = new ArrayList<>();
    TD.getTexts(texts, getArgumentsStrict().pageBlocks);

    ArrayList<FormattedText> formattedTextsL = new ArrayList<>(texts.size());
    for (int a = 0; a < texts.size(); a++) {
      FormattedText f = FormattedText.parseRichText(this, texts.get(a), null);
      if (f != null) {
        formattedTextsL.add(f);
      }
    }

    this.formattedTexts = formattedTextsL.toArray(new FormattedText[0]);


    View v = new View(context) {
      @Override
      protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        start();
      }

      @Override
      protected void onDraw (@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText("Objects: " + formattedTexts.length, Screen.dp(50), Screen.dp(50), Paints.getTextPaint15());
        canvas.drawText("Layout time: " + TimeUnit.NANOSECONDS.toMillis(sum / (Math.min(count, AVERAGE) * REPEATS)) + " ms", Screen.dp(50), Screen.dp(70), Paints.getTextPaint15());
        canvas.drawText("Layout time (min): " + TimeUnit.NANOSECONDS.toMillis(min) + " ms", Screen.dp(50), Screen.dp(90), Paints.getTextPaint15());
      }
    };

    v.setBackgroundColor(0xFFFFFFFF);

    return v;
  }


  private static final int REPEATS = 1;
  private static final int AVERAGE = 10;

  private long[] results = new long[AVERAGE];
  private int index = 0;
  private long sum = 0;
  private int count;
  private boolean started;
  private long min = -1;

  private void start () {
    if (isDestroyed() || started) {
      return;
    }

    started = true;
    run();
  }

  private void run () {
    if (isDestroyed()) {
      return;
    }

    final int width = getValue().getMeasuredWidth();
    final Text[] texts = new Text[formattedTexts.length];
    long s = System.nanoTime();

    for (int N = 0; N < REPEATS; N++) {
      for (int a = 0; a < formattedTexts.length; a++) {
        FormattedText formattedText = formattedTexts[a];
        Text.Builder b = new Text.Builder(formattedText.text, width, PageBlockRichText.getParagraphProvider(), TextColorSets.InstantView.NORMAL).entities(formattedText.entities, null);
        texts[a] = b.build();
      }
    }

    long e = System.nanoTime();

    sum -= results[index];
    results[index] = e - s;
    sum += results[index];
    min = min == -1 ? results[index] : Math.min(min, results[index]);
    index = (index + 1) % AVERAGE;
    count++;
    getValue().invalidate();

    UI.post(this::run);
  }


  @Override
  public int getId () {
    return 0;
  }
}
