import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class PicturePuzzle implements ActionListener {
    public static void main(String[] args) {
        new PicturePuzzle();
    }

    private JFrame frame;
    private JFrame winFrame;
    private JButton targetImage;
    private JButton[][] gridButtons;
    private JLabel moveCounter;
    private int[][] tileIcons = new int[3][3];
//    private String imageRoot = "src/pictures/picture";
    private String imageRoot = "pictures/picture";
    private String imageFileName;
    private Random random = new Random();
    private ImageIcon image;
    private final int mainImageWidth = 100;
    private final int mainImageHeight = 100;
    private int numberOfMoves ;
    private JLabel winMessage;
    private int firstRow;
    private int firstColumn;
    private int secondRow;
    private int secondColumn;

    public PicturePuzzle() {
        frame = new JFrame("Picture Puzzle");

        Font font = new Font("Arial", Font.PLAIN, 15);

        JLabel instructions = new JLabel();
        instructions.setText("Click two adjacent tiles adjacent to swap.");
        instructions.setBounds(20, 10, 300, 30);
        instructions.setFont(font);
        frame.add(instructions);

        JLabel targetLabel = new JLabel("Target Image:");
        targetLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        targetLabel.setBounds(350,120, 100,30);
        frame.add(targetLabel);

        setTargetImage();

        gridButtons = new JButton[3][3];
        newGrid();

        JButton deselectButton = new JButton("Deselect");
        deselectButton.setFont(font);
        deselectButton.setBounds(350,320,100,30);
        deselectButton.addActionListener(this);
        frame.add(deselectButton);

        moveCounter = new JLabel();
        setMoveCounter();
        moveCounter.setFont(font);
        moveCounter.setBounds(20, 370, 300, 30);
        frame.add(moveCounter);

        JButton newGameButton = new JButton("New Game");
        newGameButton.setFont(font);
        newGameButton.setBounds(350, 370, 100, 30);
        newGameButton.addActionListener(this);
        frame.add(newGameButton);

        frame.setLayout(null);
        frame.setSize(500,450);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        numberOfMoves = 0;
        firstRow = -1;
        firstColumn = -1;
        secondRow = -1;
        secondColumn = -1;

        winFrame = new JFrame();

        winMessage = new JLabel();
        winMessage.setBounds(10,10,290,30);
        winMessage.setFont(font);

        winFrame.add(winMessage);
        winFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        winFrame.setSize(300, 80);
        winFrame.setLayout(null);
        winFrame.setVisible(false);
    }

    private void setMoveCounter() {
        moveCounter.setText("Number of moves: " + numberOfMoves);
    }

    private void setTargetImage() {
        imageFileName = imageRoot + random.nextInt(1,7)+".jpg";
        image = setMainImage(imageFileName);
        if(targetImage == null) {
            targetImage = new JButton(image);
            targetImage.setBounds(350,150,mainImageWidth,mainImageHeight);
            frame.add(targetImage);
        }
        else
            targetImage.setIcon(image);
    }

    private ImageIcon setMainImage(String fileName){
        InputStream stream = PicturePuzzle.class.getResourceAsStream(fileName);
        try {
            assert stream != null;
            return new ImageIcon(ImageIO
                    .read(stream)
                    .getScaledInstance(mainImageWidth, mainImageHeight, Image.SCALE_DEFAULT));
        }
        catch(IOException excp){
            return new ImageIcon();
        }
    }

    private void newGrid() {
        final int xOrigin = 20;
        final int yOrigin = 50;
        final int xSize = 100;
        final int ySize = 100;
        final int totalHeight = 300;
        final int totalWidth = 300;
        int xPosition;
        int yPosition;
        BufferedImage fullImage= new BufferedImage(totalWidth, totalHeight, Image.SCALE_DEFAULT);

        boolean[][] partUsed = new boolean[3][3];
        int widthOffset;
        int heightOffset;

        InputStream stream = PicturePuzzle.class.getResourceAsStream(imageFileName);

        try{
            fullImage = ImageIO
                    .read(stream);
//            fullImage = ImageIO.read(new File(imageFileName));
        }
        catch (IOException excp){}

        final int imageWidth = fullImage.getWidth();
        final int imageHeight = fullImage.getHeight();
        final int tileWidth = imageWidth/3;
        final int tileHeight = imageHeight/3;

        for(int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                yPosition = yOrigin + row * ySize;
                xPosition = xOrigin + column * xSize;

                while(true) {
                    widthOffset = random.nextInt(0, 3);
                    heightOffset = random.nextInt(0, 3);
                    if(!partUsed[widthOffset][heightOffset]) {
                        partUsed[widthOffset][heightOffset] = true;
                        break;
                    }
                }
                BufferedImage partImage
                        = fullImage.getSubimage(widthOffset*tileWidth,heightOffset*tileHeight,tileWidth,tileHeight);

                ImageIcon icon = new ImageIcon(partImage.getScaledInstance(xSize,ySize,Image.SCALE_DEFAULT));
                if(gridButtons[row][column] == null){
                    gridButtons[row][column] = new JButton(icon);
                    gridButtons[row][column].setBounds(xPosition, yPosition, xSize, ySize);
                    gridButtons[row][column].setActionCommand(Integer.toString(row) + Integer.toString(column));
                    gridButtons[row][column].addActionListener(this);
                    gridButtons[row][column].setName("button" + row + column);
                    frame.add(gridButtons[row][column]);
                }
                else
                    gridButtons[row][column].setIcon(icon);

//                Keep track of which image is where.
                tileIcons[row][column] = heightOffset*10 + widthOffset;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        String buttonPressed = event.getActionCommand();

        if(buttonPressed == "New Game"){
            setTargetImage();
            newGrid();
            numberOfMoves = 0;
            setMoveCounter();
        }
        else if(buttonPressed == "Deselect"){
            gridButtons[firstRow][firstColumn].setEnabled(true);
            firstRow = -1;
            firstColumn = -1;
        }
        else if(firstRow == -1){
            firstRow = Integer.parseInt(String.valueOf(buttonPressed.charAt(0)));
            firstColumn = Integer.parseInt(String.valueOf(buttonPressed.charAt(1)));
            gridButtons[firstRow][firstColumn].setEnabled(false);
        }
        else{
            secondRow = Integer.parseInt(String.valueOf(buttonPressed.charAt(0)));
            secondColumn = Integer.parseInt(String.valueOf(buttonPressed.charAt(1)));

//          Valid swap with adjacent tiles in same row or column i.e. not diagonal.
            if((Math.abs(secondRow - firstRow) == 1
                    && Math.abs(secondColumn - firstColumn) == 0)
                    || (Math.abs(secondRow - firstRow) == 0
                    && Math.abs(secondColumn - firstColumn) == 1)) {

                Icon tempIcon = gridButtons[firstRow][firstColumn].getIcon();
                gridButtons[firstRow][firstColumn]
                        .setIcon(gridButtons[secondRow][secondColumn].getIcon());
                gridButtons[secondRow][secondColumn].setIcon(tempIcon);

                int temp = tileIcons[secondRow][secondColumn];
                tileIcons[secondRow][secondColumn] = tileIcons[firstRow][firstColumn];
                tileIcons[firstRow][firstColumn] = temp;

                numberOfMoves++;
                setMoveCounter();

                gridButtons[firstRow][firstColumn].setEnabled(true);
                firstRow = -1;
                firstColumn = -1;

                if(checkWin()) {
                    winMessage.setText("Congratulations! You won in " + numberOfMoves + " moves.");
                    winFrame.setVisible(true);
                }
            }
        }
    }

    private boolean checkWin() {
        boolean temp = true;
        for(int row = 0; row < 3; row++)
            for (int column = 0; column < 3; column++)
                temp = temp && tileIcons[row][column] == (row * 10 + column);
        return temp;
    }
}