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
    private static final String TITLE_VERSION = "MedPrep Calculator v1.6";

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
        super(TITLE_VERSION);
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
        grades = initialize(grades);
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
        getBreakdown();
        printBreakdown();
    }

    private BigDecimal[] initialize(BigDecimal[] arrayToZero){
        for (int i = 0; i < grades.length; i++) {
            arrayToZero[i] = new BigDecimal(0);
        }
        return arrayToZero;
    }

    private BigDecimal[] fixDecimal(BigDecimal[] arrayToFix){
        for (int i = 0; i < arrayToFix.length; i++){
            arrayToFix[i] = arrayToFix[i].setScale(3,RoundingMode.HALF_UP);
        }
        return arrayToFix;
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

    private BigDecimal getGPA(BigDecimal[] arrayContainingGrades){
        return getGpaPoints(arrayContainingGrades).divide(getGpaHours(arrayContainingGrades),3,RoundingMode.HALF_UP);
    }

    private BigDecimal getTenths(BigDecimal qualityHours, int letterGrade) {
        BigDecimal hoursTaken;

        if (qualityHours.compareTo(new BigDecimal(.1).setScale(3,RoundingMode.HALF_UP)) >= 0) {
            grades[letterGrade] = grades[letterGrade].add(new BigDecimal(.1).setScale(3,RoundingMode.HALF_UP));
            hoursTaken = new BigDecimal(.1);
        }
        else {
            hoursTaken = getHundredths(qualityHours, letterGrade);
        }

        return hoursTaken.setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal getHundredths(BigDecimal qualityHours, int letterGrade){
        BigDecimal hoursTaken;
        if (qualityHours.compareTo(new BigDecimal(.01).setScale(3,RoundingMode.HALF_UP)) >= 0) {
            grades[letterGrade] = grades[letterGrade].add(new BigDecimal(.01).setScale(3,RoundingMode.HALF_UP));
            hoursTaken = new BigDecimal(.01);
        }
        else {
            hoursTaken = getThousandths(letterGrade);
        }

        return hoursTaken.setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal getThousandths(int letterGrade){
        BigDecimal hoursTaken;

        grades[letterGrade] = grades[letterGrade].add(new BigDecimal(.001).setScale(3,RoundingMode.HALF_UP));
        hoursTaken = new BigDecimal(.001);

        return hoursTaken.setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal[] getCarryover(){
        BigDecimal trueGPA = getGPA(GPA_HOURS,GPA_POINTS).setScale(3,RoundingMode.HALF_UP);
        BigDecimal[] carryover = new BigDecimal[6];
        for (int i = 0; i < carryover.length; i++){
            carryover[i] = new BigDecimal(0);
        }
        //Number of F's required
        carryover[F] = (GPA_HOURS.subtract(AWARD_HOURS).compareTo(BigDecimal.ZERO) > 0)? GPA_HOURS.subtract(AWARD_HOURS): BigDecimal.ZERO;

        if (trueGPA.multiply(new BigDecimal(2)).compareTo(new BigDecimal(A).setScale(3,RoundingMode.HALF_UP)) >= 0){
            carryover[A] = carryover[F];
        }
        else if (trueGPA.multiply(new BigDecimal(2)).compareTo(new BigDecimal(B).setScale(3,RoundingMode.HALF_UP)) >= 0){
            carryover[B] = carryover[F];
        }
        else if (trueGPA.multiply(new BigDecimal(2)).compareTo(new BigDecimal(C).setScale(3,RoundingMode.HALF_UP)) >= 0){
            carryover[C] = carryover[F];
        }
        else if (trueGPA.multiply(new BigDecimal(2)).compareTo(new BigDecimal(D).setScale(3,RoundingMode.HALF_UP)) >= 0){
            carryover[D] = carryover[F];
        }

        return carryover;
    }

    private BigDecimal getGpaHours(BigDecimal[] arrayToCombine){
        return arrayToCombine[A].add(arrayToCombine[B]).add(arrayToCombine[C]).add(arrayToCombine[D]).add(arrayToCombine[F]).setScale(3,RoundingMode.HALF_UP);
    }

    private BigDecimal getGpaPoints(BigDecimal[] arrayToEvaluate){
        BigDecimal as = new BigDecimal(A);
        BigDecimal bs = new BigDecimal(B);
        BigDecimal cs = new BigDecimal(C);
        BigDecimal ds = new BigDecimal(D);
        return arrayToEvaluate[A].multiply(as).add(arrayToEvaluate[B].multiply(bs)).add(arrayToEvaluate[C].multiply(cs))
                .add(arrayToEvaluate[D].multiply(ds)).setScale(3,RoundingMode.HALF_UP);
    }

    private BigDecimal getAwardHours(BigDecimal[] arrayToEvaluate){
        BigDecimal awarded = getGpaHours(arrayToEvaluate);
        return awarded.subtract(arrayToEvaluate[F]).add(arrayToEvaluate[CR]).setScale(3,RoundingMode.HALF_UP);
    }

    /**
     * Determines if computed hour breakdown matches original totals.
     * This includes GPA points.
     * @param arrayToCompare An array containing grades.
     * @return Returns a boolean value based upon whether or not the new totals and original totals match.
     */
    private boolean totalsMatch(BigDecimal[] arrayToCompare){
        return (getAwardHours(arrayToCompare).compareTo(AWARD_HOURS.setScale(3,RoundingMode.HALF_UP)) == 0 &&
                getGpaHours(arrayToCompare).compareTo(GPA_HOURS.setScale(3,RoundingMode.HALF_UP)) == 0 &&
                getGpaPoints(arrayToCompare).compareTo(GPA_POINTS.setScale(3,RoundingMode.HALF_UP)) == 0);
    }

    /**
     * Overload of getBreakdown(int)
     */
    private void getBreakdown(){
        BigDecimal[] newBigD = new BigDecimal[6];
        for (int i = 0; i < newBigD.length; i++){
            newBigD[i] = new BigDecimal(0);
        }
        getBreakdown(1);
    }

    /**
     * Primary calculation logic for determining grade breakdown.
     * @param safety Prevents getBreakdown from running too many times
     */
    private void getBreakdown(int safety){
        breakdown = "";
        grades = initialize(grades);
        if (safety > 3){
            breakdown = "Error: Safety out of bounds.";
        }
        else {
            //Make sure GPA isn't too high.
            if (getGPA(GPA_HOURS, GPA_POINTS).compareTo(MAXIMUM_SIU_GPA.setScale(3, RoundingMode.HALF_UP)) > 0) {
                breakdown = "GPA is too high." +
                        "\nCheck the GPA Points or GPA Hours and try again.";
            } else {
                //Do the stuff
                //Check for F's up front
                BigDecimal[] carryover = getCarryover();
                System.arraycopy(carryover, 0, grades, 0, grades.length);
                BigDecimal newGPA_Hours = GPA_HOURS.subtract(getGpaHours(carryover));
                BigDecimal newGPA_Points = GPA_POINTS.subtract(getGpaPoints(carryover));
                BigDecimal newAward_Hours = AWARD_HOURS.subtract(getAwardHours(carryover));

                if (getGPA(newGPA_Hours,newGPA_Points).compareTo(MAXIMUM_SIU_GPA) > 0){
                    breakdown = "Awarded Hours might be too low.\nCheck your totals and try again.";
                }
                else {

                    for (int i = 4; i > 0; i--) {
                        while (getGPA(newGPA_Hours, newGPA_Points).compareTo(new BigDecimal(i - 1).setScale(3, RoundingMode.HALF_UP).stripTrailingZeros()) > 0
                                && newGPA_Hours.compareTo(new BigDecimal(.001).setScale(3, RoundingMode.HALF_UP)) >= 0) {
                            //Check hour breakdown using tenths, hundredths, or thousandths
                            BigDecimal hoursTaken = getTenths(newGPA_Hours, i);
                            //Take out the hours and points you've added to grades[]
                            newGPA_Points = newGPA_Points.subtract(hoursTaken.multiply(new BigDecimal(i)));
                            newGPA_Hours = newGPA_Hours.subtract(hoursTaken);
                            newAward_Hours = newAward_Hours.subtract(hoursTaken);
                        }
                    }

                    if (safety <= 1) {
                        grades[F] = (newGPA_Hours.compareTo(BigDecimal.ZERO) >= 0) ? grades[F].add(newGPA_Hours) : BigDecimal.ZERO;
                    }
                    //Determine number of CR's
                    grades[CR] = (newAward_Hours.compareTo(BigDecimal.ZERO) >= 0) ? grades[CR].add(newAward_Hours) : BigDecimal.ZERO;

                    grades = fixDecimal(grades);

                    if (safety <= 2 && !totalsMatch(grades)) {
                        getBreakdown(++safety);
                    } else {
                        if (!totalsMatch(grades) && !fallbackBreakdown(AWARD_HOURS,GPA_HOURS,GPA_POINTS)) {
                            breakdown = "Error: Unable to determine grade breakdown.";
                        }
                    }
                }
            }
        }
    }

    /**
     * Performs a simple, division-based breakdown of hours.
     * This method is not always accurate, but it is a failsafe for unaccounted-for hour/point combinations.
     * @param awardHours Hours received minus F's and plus CR's.
     * @param gpaHours Hours received that carry grade point values.
     * @param gpaPoints Grade points received.
     * @return Returns a boolean value based upon whether or not the simple division was successful.
     */
    private boolean fallbackBreakdown(BigDecimal awardHours, BigDecimal gpaHours, BigDecimal gpaPoints){
        grades = initialize(grades);
        for (int i = 4; i > 0; i--){
            if (getGPA(GPA_HOURS,GPA_POINTS).compareTo(new BigDecimal(i - 1).setScale(3, RoundingMode.HALF_UP)) >= 0){
                grades[i] = gpaPoints.divide(new BigDecimal(i),3,RoundingMode.HALF_UP);
                gpaPoints = gpaPoints.subtract(grades[i].multiply(new BigDecimal(i).setScale(3,RoundingMode.HALF_UP)).setScale(3,RoundingMode.HALF_UP));
                gpaHours = gpaHours.subtract(grades[i]).setScale(3,RoundingMode.HALF_UP);
                awardHours = awardHours.subtract(grades[i]).setScale(3,RoundingMode.HALF_UP);
            }
            grades[F] = (gpaHours.compareTo(BigDecimal.ZERO) >= 0) ? grades[F].add(gpaHours) : BigDecimal.ZERO;
            //Determine number of CR's
            grades[CR] = (awardHours.compareTo(BigDecimal.ZERO) >= 0) ? grades[CR].add(awardHours) : BigDecimal.ZERO;
        }
        return totalsMatch(grades);
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
            grades = fixDecimal(grades);

            //Convert quarter hours to semester hours if indicated.
            if (qtrCheckBox.isSelected()) {
                for (int i = 0; i < grades.length; i++) {
                    grades[i] = grades[i].multiply(QTR_RATE).setScale(3, RoundingMode.HALF_UP);
                }
            }
            String[] letterGrade = {"F","D","C","B","A","CR"};
            for (int i = 4; i > -1; i--){
                if (grades[i].compareTo(BigDecimal.ZERO) > 0)
                    breakdown = breakdown.concat("Number of " + letterGrade[i] + "'s: " + grades[i].stripTrailingZeros().toPlainString() + "\n");
            }
            if (grades[CR].compareTo(BigDecimal.ZERO) > 0) //CR's
                breakdown = breakdown.concat("Number of CR's: " + grades[CR].stripTrailingZeros().toPlainString() + "\n");
            outputField.setText("GPA: " + getGPA(grades) + "\n" + breakdown);
        }
    }
}