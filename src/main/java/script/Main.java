package script;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main {
	
	private static String bookCab(String username, String password, String destination, Map<String, String> userResponse){
		CabBooking cb = new CabBooking(username, password, destination, userResponse);
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Future<String> future = executor.submit(cb);
		List<Future<String>> futureList = new ArrayList<Future<String>>();
		futureList.add(future);
		String response = "";
		for (Future<String> f : futureList) {
			try {
				response = f.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		return response;
		
	}
	
	private static boolean toContinue(String isSuccessful, int count) {
		if (isSuccessful.contains("Booked Succesfully") || isSuccessful.contains("Your seat is already booked")
				|| isSuccessful.contains("Successfully cancelled booking")
				|| isSuccessful.contains("Sorry! it seems that you have not yet booked your cab.")) {
			return false;
		} else if (count == 3) {
			return false;
		}
		return true;
	}
	
	
	public static void main(String[] args) throws InterruptedException {
		/*
		 *provide username and password either by command line argumment 
		 *or hardcode it into programe
		 */
		String destination = "";
		String username = "";
		String password = "\"\"";
		if (args.length == 0) {
			System.out.println("No password is provided. Will use system default.");
		} else if (args.length != 2) {
			System.out.println("Please provide both username and password. Will use system default");
		} else {
			username = args[0];
			password = args[1];
		}

		System.out.println("===================================================\n" + "VIKHROLI CAB BOOKING SERVICE\n"
				+ "===================================================\n\n");

		String userSelectedTime = "";
		Map<String, String> userResponse = new HashMap<>();
		ExecutorService es = Executors.newFixedThreadPool(1);
		Future<Map<String, String>> future1 = es.submit(() -> {
			Map<String, String> userInput = new HashMap<String, String>();
			Scanner sc = new Scanner(System.in);
			System.out.println("Where you wanna go?");
			System.out.println("1:Vikhroli \n2:Kanjumarg");
			int destinationChoice = sc.nextInt();
			String localDestination = getDestination(destinationChoice);
			userInput.put("destination", localDestination);
			System.out.println("What you wanna do ?\n");
			System.out.println("1: Book Cab\n" + "2: Cancel Cab");
			int userChoice = sc.nextInt();
			String operation = "";
			switch (userChoice) {
			case 1:
				operation = "Book Cab";
				break;
			case 2:
				operation = "Cancel Cab";
				break;
			default:
				System.out.println("Invalid choice");
				System.exit(0);
			}
			userInput.put("operation", operation);
			String time = "";
			if ("Book Cab".equalsIgnoreCase(operation)) {
				System.out.println("Please select time? ");
				if(destinationChoice == 1){
					System.out.println("1: 5:15PM\n" + "2: 6:00PM\n" + "3: 7:15PM\n" + "4: 8:00PM");
					int choice = sc.nextInt();
					time = mappingForVikhroli(choice);
				}else{
					System.out.println("1: 5:30PM\n" + "2: 6:15PM\n" + "3: 7:30PM\n" + "4: 8:45PM");
					int choice = sc.nextInt();
					time = mappingForKanjurmarg(choice);
				}
				userInput.put("time", time);
			}
			sc.close();
			return userInput;
		});
		try {
			try {
				userResponse = future1.get(25, TimeUnit.SECONDS);
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				System.out.println("Timeout has occured!");
			}
		} finally {
			if (userResponse.size() != 0) {
				if("".equalsIgnoreCase(userResponse.get("operation"))){
					userResponse.put("operation", "Book Cab");
				}
				if ("Book Cab".equalsIgnoreCase(userResponse.get("operation"))) {
					if ("".equalsIgnoreCase(userResponse.get("time")) || userResponse.get("time") == null) {
						if ("Vikhroli".equalsIgnoreCase(userResponse.get("destination"))) {
							System.out.println("Using system default time : 7:15 PM");
							userSelectedTime = "07:15 PM";
						} else {
							System.out.println("Using system default time : 7:30 PM");
							userSelectedTime = "07:30 PM";
						}
						userResponse.put("time", userSelectedTime);
					}
				}
			} else {
				System.out.println("Going to book a cab using system default time : 7:15 PM");
				userResponse.put("destination", "Vikhroli");
				userResponse.put("operation", "Book Cab");
				userResponse.put("time", "08:00 PM");
			}

		}
		System.out.println(userResponse.get("destination"));
		System.out.println(userResponse.get("operation"));
		System.out.println(userResponse.get("time"));
		int count = 0;
		String isSuccessful = "";
		//System.out.println("Username is " + username);
		//System.out.println("Password is " + password);
		do {
			System.out.println(isSuccessful);
			isSuccessful = Main.bookCab(username, password, userResponse.get("destination"), userResponse);
			if (isSuccessful.contains("Can not capture screenshot!")) {
				System.out.println("Failed to capture screenshot!");
				break;
			} else if (isSuccessful.contains("Login Failed")) {
				count++;
			}
		} while (toContinue(isSuccessful, count));
		System.out.println(isSuccessful);
		System.exit(0);
	}

	private static String getDestination(int destinationChoice) {
		if(destinationChoice == 1){
			return "Vikhroli";
		}else if(destinationChoice == 2){
			return "Kanjurmarg";
		}else{
			System.out.println("Please select appropriate destination!!!");
			System.exit(0);
		}
		return "invalid";
	}
	
	private static String mappingForKanjurmarg(int choice) {
		String time = "";
		switch (choice) {
		case 1:
			time = "05:30 PM";
			break;
		case 2:
			time = "06:15 PM";
			break;
		case 3:
			time = "07:30 PM";
			break;
		case 4:
			time = "08:45 PM";
			break;
		default:
			System.out.println("Please select appropriate time!!!");
			System.exit(0);
		}
		return time;

	}

	private static String mappingForVikhroli(int choice) {
		String time = "";
		switch (choice) {
		case 1:
			time = "05:15 PM";
			break;
		case 2:
			time = "06:00 PM";
			break;
		case 3:
			time = "07:15 PM";
			break;
		case 4:
			time = "08:00 PM";
			break;
		default:
			System.out.println("Please select appropriate time!!!");
			System.exit(0);
		}
		return time;
	}

}
