package com.astamuse.asta4d.web.test.form.field;

import org.testng.annotations.Test;

import com.astamuse.asta4d.render.Renderer;
import com.astamuse.asta4d.web.form.field.impl.InputHiddenRenderer;
import com.astamuse.asta4d.web.test.WebTestBase;
import com.astamuse.asta4d.web.test.form.FormRenderCase;

public class InputHiddenTest extends WebTestBase {
    public static class TestSnippet {
        public Renderer normalEditValue() {
            FieldRenderBuilder builder = FieldRenderBuilder.of(InputHiddenRenderer.class);
            builder.addValue("nullvalue", null);
            builder.addValue("emptyvalue", "");
            builder.addValue("xvalue", "x");
            return builder.toRenderer(true);
        }

        public Renderer normalDisplayValue() {
            FieldRenderBuilder builder = FieldRenderBuilder.of(InputHiddenRenderer.class);
            builder.addValue("nullvalue", null);
            builder.addValue("emptyvalue", "");
            builder.addValue("xvalue", "x");
            return builder.toRenderer(false);
        }
    }

    @Test
    public void test() {
        new FormRenderCase("/InputHidden.html");
    }
}