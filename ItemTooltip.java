import java.awt.*;

public class ItemTooltip{
    private static final int PADDING = 4;
    private static final int MAX_WIDTH = 100;
    private static final Color BACKGROUND_COLOR = new Color(0, 0, 0, 230);
    private static final Color BORDER_COLOR = new Color(255, 255, 255, 150);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 8);
    private static final Font DESC_FONT = new Font("Arial", Font.BOLD, 6);
    private static final long TOOLTIP_DURATION = 3000; // 3 seconds
    
    private final String title;
    private final String description;
    private final boolean isConsumable;
    private int x, y;
    private final float alpha = 0.7f;
    private boolean showTooltips;
    private long showStartTime;
    private boolean isVisible;
    private Room currentRoom;
    
    public ItemTooltip(String title, String description, boolean isConsumable) {
        this.title = title;
        this.description = description;
        this.isConsumable = isConsumable;
        showTooltips = false;
        isVisible = false;
    }
    
    public boolean getShowTooltips() {
        return showTooltips;
    }

    public String getTitle() {
        return title;
    }

    public void setShowTooltips(boolean showTooltips) {
        if (showTooltips && !this.showTooltips) {
            // Start timer when tooltip is shown
            showStartTime = System.currentTimeMillis();
            isVisible = true;
        }
        this.showTooltips = showTooltips;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public float getAlpha() {
        return alpha;
    }

    public void hideTooltip() {
        showTooltips = false;
        isVisible = false;
    }

    public void update(Room currentRoom) {
        // Check if room has changed
        if (this.currentRoom != null && currentRoom != null && this.currentRoom != currentRoom) {
            hideTooltip();
        }
        this.currentRoom = currentRoom;

        // Check timer
        if (isVisible && System.currentTimeMillis() - showStartTime > TOOLTIP_DURATION) {
            hideTooltip();
        }
    }

    private String[] wrapText(String text, FontMetrics metrics, int maxWidth) {
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        java.util.List<String> lines = new java.util.ArrayList<>();
        
        for (String word : words) {
            if (metrics.stringWidth(currentLine + " " + word) <= maxWidth) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines.toArray(new String[0]);
    }
    

    public void draw(Graphics2D g2d, int xOffset, int yOffset) {
        if (!isVisible || alpha <= 0) return;
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Calculate tooltip dimensions
        FontMetrics titleMetrics = g2d.getFontMetrics(TITLE_FONT);
        FontMetrics descMetrics = g2d.getFontMetrics(DESC_FONT);
        
        int titleWidth = titleMetrics.stringWidth(title);
        int descWidth = Math.min(descMetrics.stringWidth(description), MAX_WIDTH - PADDING * 2);
        
        // Wrap description text
        String[] wrappedDesc = wrapText(description, descMetrics, MAX_WIDTH - PADDING * 2);
        int descHeight = wrappedDesc.length * descMetrics.getHeight();
        
        int totalWidth = Math.max(titleWidth, descWidth) + PADDING * 2;
        int totalHeight = titleMetrics.getHeight() + descHeight + PADDING * 6;
        
        // Draw semi-transparent background
        g2d.setColor(new Color(BACKGROUND_COLOR.getRed(), BACKGROUND_COLOR.getGreen(), 
                             BACKGROUND_COLOR.getBlue(), (int)(BACKGROUND_COLOR.getAlpha() * alpha)));
        g2d.fillRoundRect(x, y, totalWidth, totalHeight, 10, 10);
        
        // Draw border
        g2d.setColor(new Color(BORDER_COLOR.getRed(), BORDER_COLOR.getGreen(), 
                             BORDER_COLOR.getBlue(), (int)(BORDER_COLOR.getAlpha() * alpha)));
        g2d.drawRoundRect(x, y, totalWidth, totalHeight, 10, 10);
        
        // Draw title
        g2d.setColor(new Color(TEXT_COLOR.getRed(), TEXT_COLOR.getGreen(), 
                             TEXT_COLOR.getBlue(), (int)(TEXT_COLOR.getAlpha() * alpha)));
        g2d.setFont(TITLE_FONT);
        g2d.drawString(title, x + PADDING, y + PADDING + titleMetrics.getAscent());
        
        // Draw description
        g2d.setFont(DESC_FONT);
        int currentY = y + PADDING * 2 + titleMetrics.getHeight();
        for (String line : wrappedDesc) {
            g2d.drawString(line, x + PADDING, currentY + descMetrics.getAscent());
            currentY += descMetrics.getHeight();
        }
        
        // Draw item type indicator
        String typeText = isConsumable ? "Consumable" : "Equipment";
        g2d.setColor(new Color(220, 220, 220, (int)(220 * alpha)));
        g2d.drawString(typeText, x + PADDING, currentY + PADDING * 2);
    }

} 