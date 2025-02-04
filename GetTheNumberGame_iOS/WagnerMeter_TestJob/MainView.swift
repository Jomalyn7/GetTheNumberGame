//
//  MainView.swift
//  WagnerMeter_TestJob
//
//  Created by Ma. Jomalyn O. Espena on 2/3/25.
//

import SwiftUI
import Firebase

struct MainView: View {
    
    @State private var currentNumber: Int = 1
    @State private var targetNumber: Int = 238
    @State private var totalTaps: Int = 0
    @State private var showAlert = false
    @State private var showHistory = false

    let operations: [(label: String, color: Color, operation: (Int) -> Int)] = [
        ("+1", .red, { $0 + 1 }),
        ("-1", .orange, { $0 - 1 }),
        ("×2", .yellow, { $0 * 2 }),
        ("÷2", .green, { $0 / 2 }),
        ("×10", .blue, { $0 * 10 }),
        ("÷5", .purple, { $0 / 5 })
    ]
    
    var body: some View {
        
        VStack(spacing: 20) {
            
            ZStack {
                Rectangle()
                    .fill(Color.pink.opacity(0.8))
                    .frame(height: 60)
                Text("Get the Target Number Game")
                    .font(.title3)
                    .fontWeight(.bold)
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)
            }
            .padding(.top, 16)
            
            Spacer()
            
            Text("Can you get this number?")
                .font(.title2)
            
            HStack(spacing: 8) {
                Text("Target:")
                    .font(.title2)
                    .foregroundColor(.gray)
                Text("\(targetNumber)")
                    .font(.title)
                    //.foregroundColor(.red)
                    .foregroundColor(targetNumber == currentNumber ? .green : .red)
            }
            
            HStack(spacing: 8) {
                Text("You got:")
                    .font(.title2)
                    .foregroundColor(.gray)
                Text("\(currentNumber)")
                    .font(.title)
            }
            
            Spacer()
            
            VStack(spacing: 15) {
                ForEach(operations, id: \.label) { operation in
                    Button(action: {
                        currentNumber = operation.operation(currentNumber)
                        totalTaps += 1
                        if currentNumber == targetNumber {
                            showAlert = true
                        }
                        print("Button clicked: \(operation.label)")
                        logButtonClick(buttonColor: operation.color)
                    }) {
                        ZStack {
                            RoundedRectangle(cornerRadius: 15)
                                .fill(operation.color)
                                .frame(height: 45)
                            Text(operation.label)
                                .foregroundColor(.white)
                                .font(.title2)
                        }
                    }
                }
            }
            .padding(.horizontal)
            
            Text("Total Taps: \(totalTaps)")
                .foregroundColor(.gray)
                .padding(.top)
            
            Button("See Tap Logs") {
                showHistory = true
            }
            .foregroundColor(.pink)
            .padding(.bottom)
            
            Spacer()
        }
        .overlay(
            Group {
                if showHistory {
                    ZStack {
                        Color.black.opacity(0.4)
                            .edgesIgnoringSafeArea(.all)
                        HistoryView(isPresented: $showHistory, username: username, buttonPresses: [])
                            .background(Color.white)
                            .cornerRadius(10)
                            .padding()
                            .transition(.move(edge: .bottom))
                            .animation(.easeInOut(duration: 0.3), value: showHistory)
                    }
                }
            }
        )
        
        .alert(totalTaps < 10 ? "Congratulations!" : "Cool!", isPresented: $showAlert) {
            Button("New Game") {
                showAlert = false
                targetNumber = Int.random(in: 2...999)
                totalTaps = 0
                currentNumber = 1
                
             }
        } message: {
            Text("You reached the target number in \(totalTaps) moves!")
        }
        
        .onAppear {
            targetNumber = Int.random(in: 2...999)
            username = loadUsername()
        }

    }
    
    func logUserEvent(userId: String, eventName: String) {
        let database = Database.database()
        let ref = database.reference().childByAutoId()
        
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        
        let eventData: [String: Any] = [
            "eventname": eventName,
            "userid": userId,
            "timestamp": dateFormatter.string(from: Date())
        ]
        
        ref.setValue(eventData) { error, _ in
            if let error = error {
                print("Error logging event: \(error.localizedDescription)")
            } else {
                print("Event logged successfully!")
            }
        }
    }
    
    func logButtonClick(buttonColor: Color) {
        let buttonColorName = getButtonColorName(buttonColor: buttonColor)
        logUserEvent(userId: username, eventName: buttonColorName)
    }

    func getButtonColorName(buttonColor: Color) -> String {
        switch buttonColor {
            case .red: return "Red"
            case .orange: return "Orange"
            case .yellow: return "Yellow"
            case .green: return "Green"
            case .blue: return "Blue"
            case .purple: return "Purple"
            default: return "Unknown"
        }
    }

  
}

#Preview {
    MainView()
}
