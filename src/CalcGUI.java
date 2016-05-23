import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    private static BigDecimal AWARD_HOURS;
    private static BigDecimal GPA_HOURS;
    private static BigDecimal GPA_POINTS;
    private static final BigDecimal QTR_RATE = new BigDecimal(.667);

    //Grade point values
    //Also array indices
    private static final int A = 4;
    private static final int B = 3;
    private static final int C = 2;
    private static final int D = 1;
    private static final int F = 0;
    private static final BigDecimal MAXIMUM_SIU_GPA = new BigDecimal(4.0);

    //Just a placeholder for readability
    private static final int CR = 5;

    //Stores hour breakdown of grades
    private BigDecimal[] grades;

    //Output string
    private String breakdown;

    CalcGUI(){
        super("MedPrep Calculator v1.5.2");
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
        grades = new BigDecimal[6]; //A's = 0, B's = 1, C's = 2, D's = 3, F's = 4, CR's = 5
        for (int i = 0; i < grades.length; i++) {
            grades[i] = new BigDecimal(0);
        }
        breakdown = "";

    }

    /**
     * Reads the program's field values and starts the calculation logic.
     */
    private void calculateGo(){
        if (!awardedHours.getText().isEmpty()){
            AWARD_HOURS = new BigDecimal(awardedHours.getText()).setScale(3,RoundingMode.HALF_UP);
        }
        else {
            awardedHours.setText("0");
            AWARD_HOURS = new BigDecimal(0);
        }
        if (!qualityHours.getText().isEmpty()){
            GPA_HOURS = new BigDecimal(qualityHours.getText()).setScale(3,RoundingMode.HALF_UP);
        }
        else {
            qualityHours.setText("0");
            GPA_HOURS = new BigDecimal(0);
        }
        if (!qualityPoints.getText().isEmpty()){
            GPA_POINTS = new BigDecimal(qualityPoints.getText()).setScale(3,RoundingMode.HALF_UP);
        }
        else {
            qualityPoints.setText("0");
            GPA_POINTS = new BigDecimal(0);
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
    private BigDecimal getGPA(BigDecimal qualityHours, BigDecimal qualityPoints){
        BigDecimal gpa = new BigDecimal(0);
        if (qualityHours.compareTo(BigDecimal.ZERO) > 0)
            gpa = qualityPoints.divide(qualityHours,3,RoundingMode.HALF_UP);
        return gpa;
    }


    private BigDecimal getPartialHoursTaken(BigDecimal qualityHours, int letterGrade) {
        BigDecimal hoursTaken;

        if (qualityHours.compareTo(new BigDecimal(.5)) == 0) {
            grades[letterGrade] = grades[letterGrade].add(new BigDecimal(.5));
            hoursTaken = new BigDecimal(.5);
        }
        else if (qualityHours.compareTo(new BigDecimal(.25)) == 0) {
            grades[letterGrade] = grades[letterGrade].add(new BigDecimal(.25));
            hoursTaken = new BigDecimal(.25);
        }
        else if (qualityHours.compareTo(new BigDecimal(.75)) == 0) {
            grades[letterGrade] = grades[letterGrade].add(new BigDecimal(.75));
            hoursTaken = new BigDecimal(.75);
        }
        else if (qualityHours.compareTo(new BigDecimal(.1)) == 0) {
            grades[letterGrade] = grades[letterGrade].add(new BigDecimal(.1));
            hoursTaken = new BigDecimal(.1);
        }
        else if (qualityHours.compareTo(new BigDecimal(.075)) == 0) {
            grades[letterGrade] = grades[letterGrade].add(new BigDecimal(.075));
            hoursTaken = new BigDecimal(.075);
        }
        else if (qualityHours.compareTo(new BigDecimal(.05)) == 0) {
            grades[letterGrade] = grades[letterGrade].add(new BigDecimal(.05));
            hoursTaken = new BigDecimal(.05);
        }
        else if (qualityHours.compareTo(new BigDecimal(.025)) == 0) {
            grades[letterGrade] = grades[letterGrade].add(new BigDecimal(.025));
            hoursTaken = new BigDecimal(.025);
        }
        else if (qualityHours.compareTo(new BigDecimal(.01)) == 0) {
            grades[letterGrade] = grades[letterGrade].add(new BigDecimal(.01));
            hoursTaken = new BigDecimal(.01);
        }
        else if (qualityHours.compareTo(new BigDecimal(.005)) == 0) {
            grades[letterGrade] = grades[letterGrade].add(new BigDecimal(.005));
            hoursTaken = new BigDecimal(.005);
        }
        else {
            grades[letterGrade] = grades[letterGrade].add(new BigDecimal(.001));
            hoursTaken = new BigDecimal(.001);
        }

        return hoursTaken;
    }

    private void getBreakdown(BigDecimal awardedHours, BigDecimal qualityHours, BigDecimal qualityPoints){
        BigDecimal[] newBigD = new BigDecimal[6];
        for (int i = 0; i < newBigD.length; i++){
            newBigD[i] = new BigDecimal(0);
        }
        getBreakdown(awardedHours, qualityHours, qualityPoints, 1, newBigD);
    }

    /**
     * Primary calculation logic for determining grade breakdown.
     * @param awardedHours Total number of hours minus F's and plus CR's
     * @param qualityHours Total number of hours used to calculate GPA
     * @param qualityPoints Total number of grade points used to calculate GPA
     * @param safety Prevents getBreakdown from running too many times
     * @param carryoverGrades An array that contains BigDecimals for use if the method is re-run
     */
    private void getBreakdown(BigDecimal awardedHours, BigDecimal qualityHours, BigDecimal qualityPoints, int safety, BigDecimal[] carryoverGrades){
        //Make sure grades[] is initialized to correct values each time.
        System.arraycopy(carryoverGrades, 0, grades, 0, grades.length);

        //Make sure GPA isn't too high.
        if (getGPA(GPA_HOURS,GPA_POINTS).compareTo(MAXIMUM_SIU_GPA) > 0){
            breakdown = "GPA is too high." +
                    "\nCheck the GPA Points or GPA Hours and try again.";
        }
        else {
            //Do the stuff
            for (int i = 4; i > 0; i--){ //Checks whole hours
                while (getGPA(qualityHours,qualityPoints).compareTo(new BigDecimal(i - 1)) > 0 && qualityHours.compareTo(BigDecimal.ONE) >= 0) {
                    grades[i] = grades[i].add(BigDecimal.ONE);
                    qualityPoints = qualityPoints.subtract(new BigDecimal(i));
                    qualityHours = qualityHours.subtract(BigDecimal.ONE);
                    awardedHours = awardedHours.subtract(BigDecimal.ONE);
                }
            }
            for (int i = 4; i > 0; i--){ //Checks partial hours
                while (getGPA(qualityHours,qualityPoints).compareTo(new BigDecimal(i-1)) > 0 && qualityHours.compareTo(new BigDecimal(.001)) >= 0) {
                    BigDecimal hoursTaken = getPartialHoursTaken(qualityHours,i);
                    qualityPoints = qualityPoints.subtract(hoursTaken.multiply(new BigDecimal(i)));
                    qualityHours = qualityHours.subtract(hoursTaken);
                    awardedHours = awardedHours.subtract(hoursTaken);
                }
            }

            //Determine number of F's
            grades[F] = (qualityHours.compareTo(BigDecimal.ZERO) >= 0)? grades[F].add(qualityHours) : BigDecimal.ZERO;
            //Determine number of CR's
            grades[CR] = (awardedHours.compareTo(BigDecimal.ZERO) >= 0) ? grades[CR].add(awardedHours) : BigDecimal.ZERO;

            //Check for the special F cases
            if (GPA_HOURS.subtract(grades[F]).add(grades[CR]).compareTo(AWARD_HOURS) > 0) {
                BigDecimal trueGPA = getGPA(GPA_HOURS,GPA_POINTS);
                BigDecimal[] carryover = new BigDecimal[6];
                for (int i = 0; i < carryover.length; i++){
                    carryover[i] = new BigDecimal(0);
                }
                carryover[F] = GPA_HOURS.subtract(AWARD_HOURS); //Number of F's required
                if (trueGPA.multiply(new BigDecimal(2)).compareTo(new BigDecimal(A)) >= 0){
                    carryover[A] = carryover[F];
                }
                else if (trueGPA.multiply(new BigDecimal(2)).compareTo(new BigDecimal(B)) >= 0){
                    carryover[B] = carryover[F];
                }
                else if (trueGPA.multiply(new BigDecimal(2)).compareTo(new BigDecimal(C)) >= 0){
                    carryover[C] = carryover[F];
                }
                else if (trueGPA.multiply(new BigDecimal(2)).compareTo(new BigDecimal(D)) >= 0){
                    carryover[D] = carryover[F];
                }
                BigDecimal subtotal = new BigDecimal(0);
                for (BigDecimal grade : carryover){
                    subtotal = subtotal.add(grade);
                }

                //Check if AWARD_HOURS was too low to being with.
                if (subtotal.compareTo(GPA_HOURS) > 0){
                    breakdown = "Awarded hours is too low." +
                            "\nDouble check and try again.";
                }
                //Otherwise re-run getBreakdown() with new inputs.
                else {
                    BigDecimal newGPA_Hours =
                            GPA_HOURS.subtract(carryover[A]).subtract(carryover[B]).subtract(carryover[C]).subtract(carryover[D]).subtract(carryover[F]);
                    BigDecimal newGPA_Points = GPA_POINTS.subtract(carryover[A].multiply(new BigDecimal(A))).subtract(carryover[B].multiply(new BigDecimal(B)))
                            .subtract(carryover[C].multiply(new BigDecimal(C))).subtract(carryover[D].multiply(new BigDecimal(D)));
                    BigDecimal newAward_Hours =
                            AWARD_HOURS.subtract(carryover[A]).subtract(carryover[B]).subtract(carryover[C]).subtract(carryover[D]);
                    if (safety > 5) {
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
            BigDecimal gpaPoints = grades[A].multiply(new BigDecimal(A)).add(grades[B].multiply(new BigDecimal(B)))
                    .add(grades[C].multiply(new BigDecimal(C))).add(grades[D].multiply(new BigDecimal(D)));
            BigDecimal gpaHours = grades[A].add(grades[B]).add(grades[C]).add(grades[D]).add(grades[F]);
            BigDecimal gpa = getGPA(gpaHours, gpaPoints);
            if (gpa.compareTo(getGPA(GPA_HOURS,GPA_POINTS)) != 0){
                outputField.setText("Error: Unable to parse grades.");
            }
            else {
                //Convert quarter hours to semester hours if indicated.
                for (int i = 0; i < grades.length; i++){
                    grades[i] = grades[i].setScale(3,RoundingMode.HALF_UP).stripTrailingZeros();
                }
                if (qtrCheckBox.isSelected()) {
                    for (int i = 0; i < grades.length; i++) {
                        grades[i] = grades[i].multiply(QTR_RATE).setScale(3, RoundingMode.HALF_UP);
                    }
                }
                if (grades[A].compareTo(BigDecimal.ZERO) > 0) //A's
                    breakdown = breakdown.concat("Number of A's: " + grades[A] + "\n");
                if (grades[B].compareTo(BigDecimal.ZERO) > 0) //B's
                    breakdown = breakdown.concat("Number of B's: " + grades[B] + "\n");
                if (grades[C].compareTo(BigDecimal.ZERO) > 0) //C's
                    breakdown = breakdown.concat("Number of C's: " + grades[C] + "\n");
                if (grades[D].compareTo(BigDecimal.ZERO) > 0) //D's
                    breakdown = breakdown.concat("Number of D's: " + grades[D] + "\n");
                if (grades[F].compareTo(BigDecimal.ZERO) > 0) //F's
                    breakdown = breakdown.concat("Number of F's: " + grades[F] + "\n");
                if (grades[CR].compareTo(BigDecimal.ZERO) > 0) //CR's
                    breakdown = breakdown.concat("Number of CR's: " + grades[CR] + "\n");
                outputField.setText("GPA: " + getGPA(gpaHours, gpaPoints) + "\n" + breakdown);
            }
        }
    }
}