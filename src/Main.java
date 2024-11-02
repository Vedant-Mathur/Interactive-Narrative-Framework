import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.control.ScrollPane;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Interactive Narrative Framework
 *
 * This application allows users to engage in an interactive narrative
 * where their choices influence the outcome of the story.
 */
public class Main extends Application {
    // Main entry point of the application
    public static void main(String[] args) {
        launch(args);
    }

    // StoryNode serves as the abstract base class for all types of story nodes
    public abstract class StoryNode {
        protected String description;
        protected StoryNode[] choices;
        protected String[] choiceTexts;

        public StoryNode(String description, String[] choiceTexts, StoryNode[] choices) {
            this.description = description;
            this.choiceTexts = choiceTexts;
            this.choices = choices;
        }

        public String getDescription() {
            return description;
        }

        public String[] getChoiceTexts() {
            return choiceTexts;
        }

        public StoryNode[] getChoices() {
            return choices;
        }
    }

    // DialogueNode is a type of StoryNode that represents dialogue choices
    public class DialogueNode extends StoryNode {
        public DialogueNode(String description, String[] choiceTexts, StoryNode[] choices) {
            super(description, choiceTexts, choices);
        }
    }

    // BattleNode is a type of StoryNode that represents battle scenarios
    public class BattleNode extends StoryNode {
        public BattleNode(String description, String[] choiceTexts, StoryNode[] choices) {
            super(description, choiceTexts, choices);
        }
    }

    // Storyline contains the structure and initialization of the story
    public class Storyline {
        private StoryNode startNode;

        public Storyline() {
            initializeStory();
        }

        private void initializeStory() {
            // Create choices and nodes
            String[] startChoices = {"Enter the cave", "Walk away"};
            StoryNode start = new DialogueNode("You stand at the entrance of a dark cave. Do you enter?", startChoices, new StoryNode[2]);

            String[] enterChoices = {"Investigate the strange sound", "Look for treasure", "Leave the cave"};
            StoryNode enterCave = new DialogueNode("The cave is cold and eerie. You hear strange noises...", enterChoices, new StoryNode[3]);

            String[] findTreasureChoices = {"Open the treasure chest", "Ignore the chest and explore further", "Leave the cave"};
            StoryNode findTreasure = new DialogueNode("You find a treasure chest glowing in the corner.", findTreasureChoices, new StoryNode[3]);

            String[] encounterChoices = {"Fight", "Flee", "Attempt to negotiate"};
            StoryNode encounterMonster = new BattleNode("A wild beast appears! Prepare to fight or flee.", encounterChoices, new StoryNode[3]);

            String[] returnChoices = {"End Story"};
            StoryNode returnHome = new DialogueNode("You decide to return home, feeling that adventure is not for you.", returnChoices, new StoryNode[1]);

            // Set up the connections
            start.getChoices()[0] = enterCave; // Enter the cave
            start.getChoices()[1] = returnHome; // Walk away

            enterCave.getChoices()[0] = new DialogueNode("You decide to investigate the strange sound coming from deeper in the cave.", new String[]{"Follow the sound deeper", "Leave the cave", "Return to the treasure chest"}, new StoryNode[3]);
            enterCave.getChoices()[1] = findTreasure; // Look for treasure
            enterCave.getChoices()[2] = returnHome; // Leave the cave

            findTreasure.getChoices()[0] = new DialogueNode("You open the treasure chest and find a magical artifact!", new String[]{"Take the artifact and leave", "Inspect the artifact further", "Close the chest and leave"}, new StoryNode[3]);
            findTreasure.getChoices()[1] = encounterMonster; // Ignore the chest and explore further
            findTreasure.getChoices()[2] = returnHome; // Leave the cave

            encounterMonster.getChoices()[0] = new DialogueNode("You bravely fight the beast and emerge victorious!", new String[]{"End Story"}, new StoryNode[1]); // Fight
            encounterMonster.getChoices()[1] = returnHome; // Flee
            encounterMonster.getChoices()[2] = new DialogueNode("You try to talk to the beast, and it surprisingly agrees to let you pass.", new String[]{"End Story"}, new StoryNode[1]); // Attempt to negotiate

            this.startNode = start;
        }

        public StoryNode getStartNode() {
            return startNode;
        }
    }

    private Storyline storyline;
    private StoryNode currentNode;
    private VBox layout;
    private Label descriptionLabel;
    private Label timerLabel; // Label to display the timer
    private VBox choicesBox;
    private Timeline choiceTimer;

    @Override
    public void start(Stage primaryStage) {
        storyline = new Storyline();
        currentNode = storyline.getStartNode();

        // Initialize JavaFX elements
        layout = new VBox(10);
        layout.setStyle("-fx-background-color: #2C3E50; -fx-padding: 20;");
        descriptionLabel = new Label();
        descriptionLabel.setTextFill(Color.WHITE);
        descriptionLabel.setFont(Font.font("Segoe UI", 16));

        // Timer Label
        timerLabel = new Label();
        timerLabel.setTextFill(Color.YELLOW);
        timerLabel.setFont(Font.font("Segoe UI", 16));

        choicesBox = new VBox(5);
        choicesBox.setAlignment(javafx.geometry.Pos.CENTER);  // Centering the choices
        ScrollPane scrollPane = new ScrollPane(choicesBox);
        scrollPane.setPrefHeight(150);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        updateStoryView();

        layout.getChildren().addAll(descriptionLabel, timerLabel, scrollPane); // Include timer label in layout
        Scene scene = new Scene(layout, 400, 300);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Interactive Story App");
        primaryStage.show();
    }

    // Update the story view and choices
    private void updateStoryView() {
        descriptionLabel.setText(currentNode.getDescription());
        choicesBox.getChildren().clear();
        timerLabel.setText(""); // Reset timer label for new choices

        // Clear any existing timer
        if (choiceTimer != null) {
            choiceTimer.stop();
        }

        // Set up choices with a timer
        final int[] timeLimit = {10}; // Use an array to keep the timeLimit effectively final
        for (int i = 0; i < currentNode.getChoiceTexts().length; i++) {
            final int choiceIndex = i; // Effectively final variable for the lambda
            String choiceText = currentNode.getChoiceTexts()[i];
            Button choiceButton = new Button(choiceText);
            choiceButton.setStyle("-fx-background-color: #2980B9; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10;");
            choiceButton.setPrefWidth(250); // Set a preferred width for buttons to keep them uniform

            choiceButton.setOnAction(event -> {
                // Run a background task when a choice is made
                Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        // Simulate loading delay
                        Thread.sleep(1000); // Simulates a loading time of 1 second
                        return null;
                    }

                    @Override
                    protected void succeeded() {
                        // Update current node and UI on success
                        currentNode = currentNode.getChoices()[choiceIndex];
                        updateStoryView();
                    }

                    @Override
                    protected void failed() {
                        showError("An error occurred while processing your choice.");
                    }
                };
                new Thread(task).start(); // Execute the task in a separate thread
            });

            choicesBox.getChildren().add(choiceButton);
        }

        // Timer countdown
        choiceTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            timeLimit[0]--; // Decrement time limit
            timerLabel.setText("Time Left: " + timeLimit[0] + " seconds"); // Update timer label
            if (timeLimit[0] <= 0) {
                handleTimeout(); // Handle the timeout action
            }
        }));
        choiceTimer.setCycleCount(Timeline.INDEFINITE); // The timeline will repeat indefinitely
        choiceTimer.play(); // Start the timeline
    }

    // Handle timeout by choosing the first available option
    private void handleTimeout() {
        timerLabel.setText("Time's up! Choosing the first option.");
        try {
            currentNode = currentNode.getChoices()[0]; // Go to the first available choice
            updateStoryView();
        } catch (Exception e) {
            showError("An error occurred during timeout handling: " + e.getMessage());
        }
    }

    // Error handling function
    private void showError(String message) {
        descriptionLabel.setText(message);
        descriptionLabel.setTextFill(Color.RED);
    }
}
