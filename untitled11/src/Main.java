import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter the number of questions (-n): ");
            int n = scanner.nextInt();
            System.out.print("Enter the range of numbers (-r): ");
            int r = scanner.nextInt();
            List<String> questions = new ArrayList<>();
            List<String> answers = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                String question = generateQuestion(r);
                String answer = calculateAnswer(question);
                questions.add(question);
                answers.add(answer);
            }
            saveToFile("Exercises.txt", questions);
            saveToFile("Answers.txt", answers);
            System.out.println("Questions and answers saved to files.");
            System.out.println("Please enter your answers (separated by spaces):");
            scanner.nextLine(); // 清除缓冲区
            String userInput = scanner.nextLine();
            String[] userAnswers = userInput.split(" ");
            checkAnswers(userAnswers, "Answers.txt");
            scanner.close(); // 关闭 Scanner
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 生成题目
    private static String generateQuestion(int range) {
        Random r = new Random();
        StringBuilder result = new StringBuilder();
        int[] num = new int[4];
        int num4 = r.nextInt(3) + 1; // 运算符数量：1, 2, 3
        int NUM = num4; // 运算符数

        char[] operators = {'+', '-', '*', '/'};
        for (int i = 0; i <= NUM; i++) {
            num[i] = r.nextInt(range);
        }
        result.append(num[0]);
        for (int i = 0; i < NUM; i++) {
            int num5 = r.nextInt(4);
            if (operators[num5] == '/' && num[i + 1] == 0) {
                num[i + 1] = r.nextInt(range - 1) + 1; // 确保分母不为零
            }
            result.append(" ").append(operators[num5]).append(" ").append(num[i + 1]);
        }
        result.append(" =");
        return result.toString();
    }

    // 计算答案
    private static String calculateAnswer(String question) {
        String expr = question.replace("=", "").trim(); // 去掉等号
        try {
            Fraction result = evaluateExpression(expr);
            return result.toString(); // 返回分数形式
        } catch (Exception e) {
            return "Error: Invalid expression";
        }
    }

    // 解析并计算表达式
    private static Fraction evaluateExpression(String expr) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expr.length()) ? expr.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            Fraction parse() {
                nextChar();
                Fraction x = parseExpression();
                if (pos < expr.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            Fraction parseExpression() {
                Fraction x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x = x.add(parseTerm()); // 加法
                    else if (eat('-')) x = x.subtract(parseTerm()); // 减法
                    else return x;
                }
            }

            Fraction parseTerm() {
                Fraction x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x = x.multiply(parseFactor()); // 乘法
                    else if (eat('/')) x = x.divide(parseFactor()); // 除法
                    else return x;
                }
            }

            Fraction parseFactor() {
                if (eat('+')) return parseFactor(); // 正号
                if (eat('-')) return new Fraction(-1, 1).multiply(parseFactor()); // 负号

                Fraction x;
                int startPos = this.pos;
                if (eat('(')) { // 括号
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // 数字
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    String numStr = expr.substring(startPos, this.pos);
                    if (numStr.contains(".")) {
                        double num = Double.parseDouble(numStr);
                        x = new Fraction((int) (num * 100), 100); // 转换为分数
                    } else {
                        x = new Fraction(Integer.parseInt(numStr), 1);
                    }
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }
                return x;
            }
        }.parse();
    }

    // 保存到文件
    private static void saveToFile(String filename, List<String> data) {
        try (FileWriter writer = new FileWriter(filename)) {
            for (String line : data) {
                writer.write(line + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
        }
    }

    // 检查答案
    private static void checkAnswers(String[] userAnswers, String answerFile) {
        List<String> correctAnswers = readFile(answerFile);

        List<Integer> correct = new ArrayList<>();
        List<Integer> wrong = new ArrayList<>();

        for (int i = 0; i < userAnswers.length; i++) {
            String userAnswer = userAnswers[i];
            String expectedAnswer = correctAnswers.get(i);

            if (userAnswer.equals(expectedAnswer)) {
                correct.add(i + 1);
            } else {
                wrong.add(i + 1);
            }
        }

        // 输出结果到 Grade.txt
        try (FileWriter writer = new FileWriter("Grade.txt")) {
            writer.write("Correct: " + correct.size() + " " + correct + "\n");
            writer.write("Wrong: " + wrong.size() + " " + wrong + "\n");
        } catch (IOException e) {
            System.err.println("Error saving Grade.txt: " + e.getMessage());
        }

        System.out.println("Check completed. Results saved to Grade.txt.");
    }

    // 读取文件
    private static List<String> readFile(String filename) {
        List<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(new java.io.File(filename))) {
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return lines;
    }
}