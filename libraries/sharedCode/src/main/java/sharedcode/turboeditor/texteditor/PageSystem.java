/*
 * Copyright (C) 2014 Vlad Mihalachi
 *
 * This file is part of Turbo Editor.
 *
 * Turbo Editor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Turbo Editor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package sharedcode.turboeditor.texteditor;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;

import sharedcode.turboeditor.preferences.PreferenceHelper;

public class PageSystem {

    private List<Page> pages;
    private PageSystemInterface pageSystemInterface;
    private int currentPage = 0;

    public PageSystem(Context context, PageSystemInterface pageSystemInterface, String text) {
        this.pageSystemInterface = pageSystemInterface;
        pages = new LinkedList<>();

        int i = 0;
        int charForPage = 15000;
        int maxLenghtInOnePage = 30000;
        int to;
        int indexOfReturn;
        int textLenght = text.length();
        boolean pageSystemEnabled = PreferenceHelper.getPageSystemEnabled(context);
        if (pageSystemEnabled && textLenght > maxLenghtInOnePage) {
            while (i < textLenght && pageSystemEnabled) {
                to = i + charForPage;
                indexOfReturn = text.indexOf("\n", to);
                if (indexOfReturn > to) to = indexOfReturn;
                if (to > text.length()) to = text.length();
                pages.add(new Page(text.substring(i, to)));
                i = to + 1;
            }


            if (i == 0)
                pages.add(new Page(""));
        } else {
            pages.add(new Page(text));
        }

        setStartingLines();
    }

    public int getStartingLine() {
        return pages.get(currentPage).getStartingLine();
    }

    public String getCurrentPageText() {
        return pages.get(currentPage).getText();
    }

    public String getTextOfNextPages(boolean includeCurrent, int nOfPages) {
        StringBuilder stringBuilder = new StringBuilder();
        int i;
        for (i = includeCurrent ? 0 : 1; i < nOfPages; i++) {
            if (pages.size() > (currentPage + i)) {
                stringBuilder.append(pages.get(currentPage + 1));
            }
        }

        return stringBuilder.toString();
    }

    public void savePage(String currentText) {
        pages.get(currentPage).setText(currentText);
    }

    public void nextPage() {
        if (!canReadNextPage()) return;
        goToPage(currentPage + 1);
    }

    public void prevPage() {
        if (!canReadPrevPage()) return;
        goToPage(currentPage - 1);
    }

    public void goToPage(int page) {
        if (page >= pages.size()) page = pages.size() - 1;
        if (page < 0) page = 0;
        boolean shouldUpdateLines = page > currentPage && canReadNextPage();
        if (shouldUpdateLines) {
            String text = getCurrentPageText();
            int nOfNewLineNow = (text.length() - text.replace("\n", "").length()) + 1; // normally the last line is not counted so we have to add 1
            int nOfNewLineBefore = pages.get(currentPage + 1).getStartingLine() - pages.get(currentPage).getStartingLine();
            int difference = nOfNewLineNow - nOfNewLineBefore;
            updateStartingLines(currentPage + 1, difference);
        }
        currentPage = page;
        pageSystemInterface.onPageChanged(page);
    }

    public void setStartingLines() {
        int i;
        int startingLine;
        int nOfNewLines;
        pages.get(0).setStartingLine(0);
        for (i = 1; i < pages.size(); i++) {
            Page page = pages.get(i - 1);
            nOfNewLines = page.getText().length() - page.getText().replace("\n", "").length() + 1;
            startingLine = page.getStartingLine() + nOfNewLines;
            pages.get(i).setStartingLine(startingLine);
        }
    }

    public void updateStartingLines(int fromPage, int difference) {
        if (difference == 0)
            return;
        int i;
        if (fromPage < 1) fromPage = 1;
        for (i = fromPage; i < pages.size(); i++) {
            int curLine = pages.get(i).getStartingLine();
            pages.get(i).setStartingLine(curLine + difference);
        }
    }

    public int getMaxPage() {
        return pages.size() - 1;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public String getAllText(String currentPageText) {
        savePage(currentPageText);
        int i;
        StringBuilder allText = new StringBuilder();
        for (i = 0; i < pages.size(); i++) {
            allText.append(pages.get(i).getText()).append("\n");
        }
        return allText.toString();
    }

    public boolean canReadNextPage() {
        return currentPage < pages.size() - 1;
    }

    public boolean canReadPrevPage() {
        return currentPage >= 1;
    }

    public interface PageSystemInterface {
        void onPageChanged(int page);
    }
}
