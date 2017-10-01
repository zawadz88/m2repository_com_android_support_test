/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.test.espresso.action;

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;

import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.remote.annotation.RemoteMsgConstructor;
import android.support.test.espresso.util.HumanReadables;
import org.hamcrest.Matcher;

/**
 * Performs whatever editor (IME) action is available on a view.
 */
public final class EditorAction implements ViewAction {

  @RemoteMsgConstructor
  public EditorAction() {}

  @Override
  public Matcher<View> getConstraints() {
    return isDisplayed();
  }

  @Override
  public String getDescription() {
    return "input method editor";
  }

  @Override
  public void perform(UiController uiController, View view) {
    EditorInfo editorInfo = new EditorInfo();
    InputConnection inputConnection = view.onCreateInputConnection(editorInfo);
    if (inputConnection == null) {
      throw new PerformException.Builder()
        .withActionDescription(this.toString())
        .withViewDescription(HumanReadables.describe(view))
        .withCause(new IllegalStateException("View does not support input methods"))
        .build();
    }

    int actionId = editorInfo.actionId != 0 ? editorInfo.actionId :
      editorInfo.imeOptions & EditorInfo.IME_MASK_ACTION;

    if (actionId == EditorInfo.IME_ACTION_NONE) {
      throw new PerformException.Builder()
        .withActionDescription(this.getDescription())
        .withViewDescription(HumanReadables.describe(view))
        .withCause(new IllegalStateException("No available action on view"))
        .build();
    }

    if (!inputConnection.performEditorAction(actionId)) {
      throw new PerformException.Builder()
        .withActionDescription(this.getDescription())
        .withViewDescription(HumanReadables.describe(view))
        .withCause(new RuntimeException(String.format(
            "Failed to perform action %#x. Input connection no longer valid", actionId)))
        .build();
    }
  }
}