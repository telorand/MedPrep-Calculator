import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * A program for getting grade breakdowns based upon GPA and hours attempted/earned.
 * Uses a greedy algorithm to determine grade breakdown.
 */
public class CalcGUI extends JFrame{
    private JPanel mpPanel;
    private JFormattedTextField awardedHours;
    private JFormattedTextField qualityHours;
    private JFormattedTextField qualityPoints;
    private JButton calculate;
    private JButton cancel;
    @SuppressWarnings("unused")
    private JLabel aHours;
    @SuppressWarnings("unused")
    private JLabel gpaHours;
    @SuppressWarnings("unused")
    private JLabel gpaPoints;
    private JTextPane outputField;
    @SuppressWarnings("unused")
    private JPanel TextPanel;
    private JCheckBox qtrCheckBox;

    private static double AWARD_HOURS;
    private static double GPA_HOURS;
    private static double GPA_POINTS;
    private static final double QTR_RATE = .667;

    //Grade point values
    private static final int A = 4;
    private static final int B = 3;
    private static final int C = 2;
    private static final int D = 1;
    private static final int F = 0;
    private static final double MAXIMUM_SIU_GPA = 4.0;

    //Just a placeholder for readability
    private static final int CR = 5;

    //Stores hour breakdown of grades
    private double[] grades;

    //Output string
    private String breakdown;

    CalcGUI(){
        super("MedPrep Calculator");
        setContentPane(mpPanel);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationByPlatform(true);

        //Set window options here:
        setSize(new Dimension(475, 475));
        setForeground(new Color(84,0,10));

        setResizable(true);
        setVisible(true);

        NumberFormat nf = NumberFormat.getNumberInstance(); // Specify specific format here.
        nf.setMaximumFractionDigits(5);
        nf.setMinimumFractionDigits(0);
        nf.setRoundingMode(RoundingMode.HALF_UP);
        NumberFormatter nff = new NumberFormatter(nf);
        DefaultFormatterFactory factory = new DefaultFormatterFactory(nff);
        awardedHours.setFormatterFactory(factory);
        qualityHours.setFormatterFactory(factory);
        qualityPoints.setFormatterFactory(factory);

        outputField.setText("Enter hours in each field as you calculated them for Bolt-On and press Calculate.\n" +
                "\nDo not convert quarter hours to semester hours." +
                "\nInstead, check the box, press Calculate, and it will do the conversion for you.");

        calculate.addActionListener(e -> calculateGo());

        cancel.addActionListener(e -> System.exit(0));
        awardedHours.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                //super.focusGained(e);
                SwingUtilities.invokeLater(() -> awardedHours.selectAll());
            }
        });
        awardedHours.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER){
                    calculate.requestFocus();
                    calculateGo();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        qualityHours.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                //super.focusGained(e);
                SwingUtilities.invokeLater(() -> qualityHours.selectAll());
            }
        });
        qualityHours.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER){
                    calculate.requestFocus();
                    calculateGo();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        qualityPoints.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                //super.focusGained(e);
                SwingUtilities.invokeLater(() -> qualityPoints.selectAll());
            }
        });
        qualityPoints.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER){
                    calculate.requestFocus();
                    calculateGo();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        //Logic here:
        grades = new double[6]; //A's = 0, B's = 1, C's = 2, D's = 3, F's = 4, CR's = 5
        breakdown = "";

    }

    /**
     * Reads the program's field values and starts the calculation logic.
     */
    private void calculateGo(){
        if (!awardedHours.getText().isEmpty()){
            AWARD_HOURS = Double.parseDouble(awardedHours.getText());
        }
        else {
            awardedHours.setText("0");
            AWARD_HOURS = 0;
        }
        if (!qualityHours.getText().isEmpty()){
            GPA_HOURS = Double.parseDouble(qualityHours.getText());
        }
        else {
            qualityHours.setText("0");
            GPA_HOURS = 0;
        }
        if (!qualityPoints.getText().isEmpty()){
            GPA_POINTS = Double.parseDouble(qualityPoints.getText());
        }
        else {
            qualityPoints.setText("0");
            GPA_POINTS = 0;
        }

        outputField.setText(null);
        breakdown = "";
        getBreakdown(AWARD_HOURS,GPA_HOURS,GPA_POINTS);
        printBreakdown();
    }

    /**
     * A method for finding out the current GPA.
     * @param qualityHours The number of hours used to calculate GPA
     * @param qualityPoints The grade points used to calculate GPA
     * @return Returns a double value of the format 1.234
     */
    private double getGPA(double qualityHours, double qualityPoints){
        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.HALF_UP);
        double gpa = 0;
        if (qualityHours != 0)
            gpa = qualityPoints / qualityHours;
        return Double.parseDouble(df.format(gpa));
    }

    /**
     * A method for ensuring that decimal values are rounded to a maximum of three places.
     * This is the maximum that Banner can handle.
     * Also used to ensure that double values don't go all squiffy (because they do).
     * Five decimal places should be the maximum any conversion would use.
     * @param inputNumber Double to be rounded.
     * @return Returns a double value of the format 0.123
     */
    private double roundToThree(double inputNumber){
        DecimalFormat df = new DecimalFormat("#.###"); //Banner can only handle 3 decimal places
        df.setRoundingMode(RoundingMode.HALF_UP);
        return Double.parseDouble(df.format(inputNumber));
    }

    private double roundToTwo(double inputNumber){
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return Double.parseDouble(df.format(inputNumber));
    }

    private double roundToOne(double inputNumber){
        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return Double.parseDouble(df.format(inputNumber));
    }

    private double getPartialHoursTaken(double qualityHours, int letterGrade) {
        int decimal = (int)(roundToTwo(qualityHours - (int)qualityHours)*100);
        double hoursTaken;

        if (decimal == 50) {
            grades[letterGrade] += .5;
            hoursTaken = .5;
        }
        else if (decimal == 25) {
            grades[letterGrade] += .25;
            hoursTaken = .25;
        }
        else if (decimal == 75){
            grades[letterGrade] += .75;
            hoursTaken = .75;
        }
        else if (decimal >= 10){
            grades[letterGrade] += .1;
            hoursTaken = .1;
        }
        else {
            int decimal2 = (int)(roundToThree(roundToThree(qualityHours) - roundToOne(qualityHours))*1000);
            if (decimal2 == 75) {
                grades[letterGrade] += .075;
                hoursTaken = .075;
            }
            else if (decimal2 == 50){
                grades[letterGrade] += .05;
                hoursTaken = .05;
            }
            else if (decimal2 == 25){
                grades[letterGrade] += .025;
                hoursTaken = .025;
            }
            else if (decimal2 >= 10){
                grades[letterGrade] += .01;
                hoursTaken = .01;
            }
            else if (decimal2 == 5){
                grades[letterGrade] += .005;
                hoursTaken = .005;
            }
            else {
                grades[letterGrade] += .001;
                hoursTaken = .001;
            }
        }
        grades[letterGrade] = roundToThree(grades[letterGrade]);
        return hoursTaken;
    }

    private void getBreakdown(double awardedHours, double qualityHours, double qualityPoints){
        getBreakdown(awardedHours, qualityHours, qualityPoints, 1, new double[6]);
    }

    /**
     * Primary calculation logic for determining grade breakdown.
     * @param awardedHours Total number of hours minus F's and plus CR's
     * @param qualityHours Total number of hours used to calculate GPA
     * @param qualityPoints Total number of grade points used to calculate GPA
     * @param safety Prevents getBreakdown from running too many times
     */
    private void getBreakdown(double awardedHours, double qualityHours, double qualityPoints, int safety, double carryoverGrades[]){
        //Make sure grades[] is initialized to correct values each time.
        System.arraycopy(carryoverGrades, 0, grades, 0, grades.length);

        //Make sure GPA isn't too high.
        if (getGPA(GPA_HOURS,GPA_POINTS) > MAXIMUM_SIU_GPA){
            breakdown = "GPA is too high." +
                    "\nCheck the GPA Points or GPA Hours and try again.";
        }
        else {
            //Do the stuff
            for (int i = 4; i > 0; i--){ //Checks whole hours
                while (getGPA(qualityHours,qualityPoints) > i - 1 && qualityHours >= 1) {
                    grades[i] += 1;
                    qualityPoints = roundToThree(qualityPoints - i);
                    qualityHours = roundToThree(qualityHours - 1);
                    awardedHours = roundToThree(awardedHours - 1);
                }
            }
            for (int i = 4; i > 0; i--){ //Checks partial hours
                while (getGPA(qualityHours,qualityPoints) > i - 1 && qualityHours >= .001) {
                    double hoursTaken = getPartialHoursTaken(qualityHours,i);
                    qualityPoints = roundToThree(qualityPoints - hoursTaken * i);
                    qualityHours = roundToThree(qualityHours - hoursTaken);
                    awardedHours = roundToThree(awardedHours - hoursTaken);
                }
            }

            //Determine number of F's
            grades[F] = (qualityHours >= 0)? roundToThree(grades[F] + qualityHours) : 0;
            //Determine number of CR's
            grades[CR] = (awardedHours >= 0) ? roundToThree(grades[CR] + awardedHours) : 0;

            //Check for the special F cases
            if (GPA_HOURS - grades[F] + grades[CR] > AWARD_HOURS) {
                double trueGPA = getGPA(GPA_HOURS,GPA_POINTS);
                double[] carryover = new double[6];
                carryover[F] = roundToThree(GPA_HOURS - AWARD_HOURS); //Number of F's required.
                if (2 * trueGPA >= A){
                    carryover[A] = carryover[F];
                }
                else if (2 * trueGPA >= B){
                    carryover[B] = carryover[F];
                }
                else if (2 * trueGPA >= C){
                    carryover[C] = carryover[F];
                }
                else if (2 * trueGPA >= D){
                    carryover[D] = carryover[F];
                }
                double subtotal = 0;
                for (double grade : carryover){
                    subtotal += grade;
                }

                //Check if AWARD_HOURS was too low to being with.
                if (roundToThree(subtotal) > GPA_HOURS){
                    breakdown = "Awarded hours is too low." +
                            "\nDouble check and try again.";
                }
                //Otherwise re-run getBreakdown() with new inputs.
                else {
                    double newGPA_Hours = roundToThree(GPA_HOURS - carryover[A] - carryover[B] - carryover[C] - carryover[D] - carryover[F]);
                    double newGPA_Points = roundToThree(GPA_POINTS - (carryover[A] * A + carryover[B] * B + carryover[C] * C + carryover[D] * D));
                    double newAward_Hours = roundToThree(AWARD_HOURS - carryover[A] - carryover[B] - carryover[C] - carryover[D]);
                    if (safety > 2) {
                        breakdown = "Error: Safety out-of bounds.";
                    }
                    else {
                        getBreakdown(newAward_Hours, newGPA_Hours, newGPA_Points, ++safety, carryover);
                    }
                }
            }
        }
    }

    /**
     * Prints the output of the program to the outputField
     */
    private void printBreakdown() {
        if (breakdown.length() != 0) {
            outputField.setText(breakdown);
        }
        else {
            outputField.setText(null);
            double gpaPoints = (grades[A] * A) + (grades[B] * B) + (grades[C] * C) + (grades[D] * D);
            double gpaHours = grades[A] + grades[B] + grades[C] + grades[D] + grades[F];
            double gpa = getGPA(gpaHours, gpaPoints);
            if (gpa != getGPA(GPA_HOURS,GPA_POINTS)){
                outputField.setText("Error: Unable to parse grades.");
            }
            else {
                //Convert quarter hours to semester hours if indicated.
                if (qtrCheckBox.isSelected()) {
                    for (int i = 0; i < grades.length; i++) {
                        grades[i] = roundToThree(grades[i] * QTR_RATE);
                    }
                }
                if (grades[A] > 0) //A's
                    breakdown = breakdown.concat("Number of A's: " + grades[A] + "\n");
                if (grades[B] > 0) //B's
                    breakdown = breakdown.concat("Number of B's: " + grades[B] + "\n");
                if (grades[C] > 0) //C's
                    breakdown = breakdown.concat("Number of C's: " + grades[C] + "\n");
                if (grades[D] > 0) //D's
                    breakdown = breakdown.concat("Number of D's: " + grades[D] + "\n");
                if (grades[F] > 0) //F's
                    breakdown = breakdown.concat("Number of F's: " + grades[F] + "\n");
                if (grades[CR] > 0) //CR's
                    breakdown = breakdown.concat("Number of CR's: " + grades[CR] + "\n");
                outputField.setText("GPA: " + getGPA(gpaHours, gpaPoints) + "\n" + breakdown);
            }
        }
    }
}
