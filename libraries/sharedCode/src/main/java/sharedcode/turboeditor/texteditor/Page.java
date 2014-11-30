package sharedcode.turboeditor.texteditor;

public class Page {
    String text;
    int startingLine = 0;

    public Page(String text) {
        this.text = text;
    }

    public int getStartingLine() {
        return startingLine;
    }

    public void setStartingLine(int startingLine) {
        this.startingLine = startingLine;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
