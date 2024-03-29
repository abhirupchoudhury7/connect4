package com.connect4;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {
	private static final int COLUMNS = 7;
	private static final int ROWS = 6;
	private static final int CIRCLE_DIAMETER = 80;
	private static final String discColor1 = "#24303E";  //defining colors of discs for two players
	private static final String discColor2 = "#4CAA88";

	private String PLAYER_ONE;
	private String PLAYER_TWO;


	private boolean isPlayerOneTurn = true; //only 1 player can play at a time so this
	private Disc[][] insertedDiscsArray = new Disc[ROWS][COLUMNS];  // For Structural Changes: For the developers.

	@FXML
	public GridPane rootGridPane;
	@FXML
	public Pane insertedDiscsPane;
	@FXML
	public Label playerNameLabel;
	@FXML
	public TextField playerOneTextField, playerTwoTextField;
	@FXML
	public Button setNamesButton;



	private boolean isAllowedToInsert = true;   // Flag to avoid same color disc being added.

	public void createPlayground() {

		Shape rectangleWithHoles = createGameStructuralGrid();
		rootGridPane.add(rectangleWithHoles, 0, 1);

		List<Rectangle> rectangleList = createClickableColumns();

		for (Rectangle rectangle: rectangleList) {
			rootGridPane.add(rectangle, 0, 1);
		}
		setNamesButton.setOnAction(event -> {
			PLAYER_ONE=playerOneTextField.getText();
			PLAYER_TWO=playerTwoTextField.getText();
			if(isPlayerOneTurn){
				playerNameLabel.setText(PLAYER_ONE);
			}
			else{
				playerNameLabel.setText(PLAYER_TWO);
			}


		});

	}

	private Shape createGameStructuralGrid() {

		Shape rectangleWithHoles = new Rectangle((COLUMNS + 1) * CIRCLE_DIAMETER, (ROWS + 1) * CIRCLE_DIAMETER);

		for (int row = 0; row < ROWS; row++) {

			for (int col = 0; col < COLUMNS; col++) {
				Circle circle = new Circle();
				circle.setRadius(CIRCLE_DIAMETER / 2);
				circle.setCenterX(CIRCLE_DIAMETER / 2);
				circle.setCenterY(CIRCLE_DIAMETER / 2);
				circle.setSmooth(true); //if circles are not smooth

				circle.setTranslateX(col * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);


				circle.setTranslateY(row * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);


				rectangleWithHoles = Shape.subtract(rectangleWithHoles, circle); //subtracting all circle from rectangleWithHoles which is taking the entire pane
			}
		}

		rectangleWithHoles.setFill(Color.WHITE);

		return rectangleWithHoles;
	}

	private List<Rectangle> createClickableColumns() {

		List<Rectangle> rectangleList = new ArrayList<>();

		for (int col = 0; col < COLUMNS; col++) {

			Rectangle rectangle = new Rectangle(CIRCLE_DIAMETER, (ROWS + 1) * CIRCLE_DIAMETER);
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setTranslateX(col * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);


			rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
			rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));

			final int column = col;
			rectangle.setOnMouseClicked(event -> {
				if (isAllowedToInsert) {
					isAllowedToInsert = false;  // When disc is being dropped then no more disc will be inserted
					insertDisc(new Disc(isPlayerOneTurn), column);
				}
			});

			rectangleList.add(rectangle); //adding rectangles that are generated from rectangleList
		}

		return rectangleList;
	}

	private void insertDisc( Disc disc, int column) {  //would insert disc in 2D array: for developers

		int row = ROWS - 1;
		while (row >= 0) { //to insert discs one over the other

			if (getDiscIfPresent(row, column) == null) //checking emptiness of array if null then insert here else decrement row
				break;

			row--;
		}

		if (row < 0)    // If it is full, we cannot insert anymore disc
			return;

		insertedDiscsArray[row][column] = disc;   // For structural Changes: For developers
		insertedDiscsPane.getChildren().add(disc);// For Visual Changes : For Players

		disc.setTranslateX(column * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);

		int currentRow = row;
		TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.5), disc); //to make it appear that disc are falling from top to bottom
		translateTransition.setToY(row * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4); //to insert disc at the bottom
		translateTransition.setOnFinished(event -> { //to change players

			isAllowedToInsert = true;   // Finally, when disc is dropped allow next player to insert disc.
			if (gameEnded(currentRow, column)) {
				gameOver();
			}

			isPlayerOneTurn = !isPlayerOneTurn; //when player 1 finishes then player 2 turn
			playerNameLabel.setText(isPlayerOneTurn? PLAYER_ONE : PLAYER_TWO);
		});

		translateTransition.play();
	}

	private boolean gameEnded(int row, int column) {
		//to get address of all rows in a particular column
		List<Point2D> verticalPoints = IntStream.rangeClosed(row - 3, row + 3)  // If, row = 3, column = 3, then range of row values= 0,1,2,3,4,5,6
				.mapToObj(r -> new Point2D(r, column))  // 0,3  1,3  2,3  3,3  4,3  5,3  6,3 --> Point2D class holds vlaue in terms of x and y coordinates
				.collect(Collectors.toList()); //converting all points to point2D objects and string in variable list

		List<Point2D> horizontalPoints = IntStream.rangeClosed(column - 3, column + 3)
				.mapToObj(col -> new Point2D(row, col))
				.collect(Collectors.toList());

		Point2D startPoint1 = new Point2D(row - 3, column + 3); //declaring starting point
		List<Point2D> diagonal1Points = IntStream.rangeClosed(0, 6)
				.mapToObj(i -> startPoint1.add(i, -i))
				.collect(Collectors.toList());

		Point2D startPoint2 = new Point2D(row - 3, column - 3);
		List<Point2D> diagonal2Points = IntStream.rangeClosed(0, 6)
				.mapToObj(i -> startPoint2.add(i, i))
				.collect(Collectors.toList());

		boolean isEnded = checkCombinations(verticalPoints) || checkCombinations(horizontalPoints)
				|| checkCombinations(diagonal1Points) || checkCombinations(diagonal2Points);

		return isEnded;
	}

	private boolean checkCombinations(List<Point2D> points) {

		int chain = 0;

		for (Point2D point: points) { //points list

			int rowIndexForArray = (int) point.getX(); //casting to integer values
			int columnIndexForArray = (int) point.getY();

			Disc disc = getDiscIfPresent(rowIndexForArray, columnIndexForArray);

			if (disc != null && disc.isPlayerOneMove == isPlayerOneTurn) {

				chain++;
				if (chain == 4) {
					return true;
				}
			} else {
				chain = 0;
			}
		}

		return false;
	}

	private Disc getDiscIfPresent(int row, int column) {    // To prevent ArrayIndexOutOfBoundException if rowIndexForArray is -1 or rowIndexForArray is -1

		if (row >= ROWS || row < 0 || column >= COLUMNS || column < 0)  // If row or column index is invalid
			return null;

		return insertedDiscsArray[row][column];
	}

	private void gameOver() {
		String winner = isPlayerOneTurn ? PLAYER_ONE : PLAYER_TWO;
		System.out.println("Winner is: " + winner);

		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Connect Four");
		alert.setHeaderText("The Winner is " + winner);
		alert.setContentText("Want to play again? ");

		ButtonType yesBtn = new ButtonType("Yes");
		ButtonType noBtn = new ButtonType("No, Exit");
		alert.getButtonTypes().setAll(yesBtn, noBtn);

		Platform.runLater(() -> { //to resolve IllegalStateException.

			Optional<ButtonType> btnClicked = alert.showAndWait();
			if (btnClicked.isPresent() && btnClicked.get() == yesBtn ) {
				// ... user chose YES so RESET the game
				resetGame();
			} else {
				// ... user chose NO .. so Exit the Game
				Platform.exit();
				System.exit(0);
			}
		});
	}

	public void resetGame() {

		insertedDiscsPane.getChildren().clear();    // Remove all Inserted Disc from Pane

		for (int row = 0; row < insertedDiscsArray.length; row++) { // Structurally, Make all elements of insertedDiscsArray[][] to null
			for (int col = 0; col < insertedDiscsArray[row].length; col++) {
				insertedDiscsArray[row][col] = null;
			}
		}

		isPlayerOneTurn = true; // Let player start the game
		playerNameLabel.setText(PLAYER_ONE);

		createPlayground(); // Prepare a fresh playground
	}

	private static class Disc extends Circle {

		private final boolean isPlayerOneMove;

		public Disc(boolean isPlayerOneMove) { //constructor
			//if player 1 is inserting disc then discColor1 else discColor2
			this.isPlayerOneMove = isPlayerOneMove;
			setRadius(CIRCLE_DIAMETER / 2);
			setFill(isPlayerOneMove? Color.valueOf(discColor1): Color.valueOf(discColor2));
			setCenterX(CIRCLE_DIAMETER/2);
			setCenterY(CIRCLE_DIAMETER/2);
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
}