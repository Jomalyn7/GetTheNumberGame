//
//  TableView.swift
//  WagnerMeter_TestJob
//
//  Created by Ma. Jomalyn O. Espena on 2/3/25.
//

import SwiftUI
import FirebaseDatabase

struct HistoryView: View {
    
    @Binding var isPresented: Bool
    let username: String
    @State var buttonPresses: [ButtonPress]
    
    var body: some View {

        GeometryReader { geometry in
        VStack {
            Text("Tap Logs for \(username)")
                .font(.title2)
                .padding(.top)
            
            ScrollView {
                VStack(spacing: 10) {
                    ForEach(buttonPresses, id: \.timestamp) { press in
                        LogEntry(buttonColor: press.buttonName, timestamp: press.timestamp)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal)
                    }
                }
                .padding(.vertical)
            }
            
            Button("Close") {
                isPresented = false
            }
            .padding(.bottom)
        }
        .background(Color(uiColor: .systemBackground))
        .cornerRadius(10)
        }

        .onAppear {
            DispatchQueue.global(qos: .background).async {
                loadData()
            }
        }
    }

    func loadData() {
        fetchFirebaseData(username) { snapshot in
            DispatchQueue.main.async {
                if let snapshot = snapshot {
                    // Convert Firebase data to ButtonPress objects
                    var presses: [ButtonPress] = []
                    for child in snapshot.children {
                        if let snap = child as? DataSnapshot,
                           let dict = snap.value as? [String: Any],
                           let buttonName = dict["eventname"] as? String,
                           let timestamp = dict["timestamp"] as? String {
                            let press = ButtonPress(
                                buttonName: buttonName,
                                timestamp: timestamp
                            )
                            presses.append(press)
                        }
                    }
                    self.buttonPresses = presses
                }
            }
        }
    }
    
    func fetchFirebaseData(_ username: String, completion: @escaping (DataSnapshot?) -> Void) {
        let database = Database.database()
        let rootRef = database.reference()
        
        rootRef.queryOrdered(byChild: "userid").queryEqual(toValue: username)
            .observeSingleEvent(of: .value) { snapshot in
                if snapshot.exists() {
                    completion(snapshot)
                } else {
                    completion(nil)
                }
            } withCancel: { error in
                print("Firebase Error: \(error.localizedDescription)")
                completion(nil)
            }
    }
}

struct ButtonPress {
    let buttonName: String
    let timestamp: String
}

struct LogEntry: View {
    
    let buttonColor: String
    let timestamp: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("\(buttonColor) button pressed")
                .font(.system(size: 18))
                .foregroundColor(getColor(buttonColor))
            Text("Time: \(timestamp)")
                .font(.system(size: 14))
        }
        .padding(.vertical, 8)
    }
}

private func formatDate(_ date: Date) -> String {
    let formatter = DateFormatter()
    formatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
    return formatter.string(from: date)
}

private func getColor(_ buttonName: String) -> Color {
    switch buttonName {
        case "Red":
            return .red
        case "Orange":
            return .orange
        case "Yellow":
            return .yellow
        case "Green":
            return .green
        case "Blue":
            return .blue
        case "Purple":
            return .purple
        default:
            return .gray
    }
}

