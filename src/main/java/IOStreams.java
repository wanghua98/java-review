import entity.Employee;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Scanner;

public class IOStreams {
    public static void main(String[] args) throws IOException {

        var staff = new Employee[3];
        staff[0] = new Employee("Carl Cracker", 75000, 1987, 12, 15);
        staff[1] = new Employee("Harry Hacker", 50000, 1989, 10, 1);
        staff[2] = new Employee("Tony Tester", 40660, 1990, 3, 15);

        try (var out = new PrintWriter("employee.dat", StandardCharsets.UTF_8)) {
            writeData(staff, out);
        }
        try (var in = new Scanner(
                new FileInputStream("employee.dat"), StandardCharsets.UTF_8)) {
            Employee[] newStaff = readData(in);

            // print the newly read employee records
            for (Employee e : newStaff) {
                System.out.println(e);
            }
        }


    }

    private static Employee[] readData(Scanner in) {
        int n = in.nextInt();
        in.nextLine();
        // read the employees
        var employees = new Employee[n];
        for (int i = 0; i < n; i++) {
            employees[i] = readEmployee(in);
        }
        return employees;
    }

    private static Employee readEmployee(Scanner in) {
        String line = in.nextLine();
        String[] tokens = line.split("\\|");
        String name = tokens[0];
        int salary = Integer.parseInt(tokens[1]);
        LocalDate hireDate = LocalDate.parse(tokens[2]);
        int year = hireDate.getYear();
        int month = hireDate.getMonthValue();
        int day = hireDate.getDayOfMonth();
        return new Employee(name, salary, year, month, day);
    }

    private static void writeData(Employee[] staff, PrintWriter out) {
        // write number of employees
        out.println(staff.length);
        // write employee records
        for (Employee e : staff) {
            writeEmployee(out, e);
        }
    }

    public static void writeEmployee(PrintWriter out, Employee e) {

        out.println(e.getName() + "|" + e.getSalary() + "|" + e.getHireDate());
    }
}
