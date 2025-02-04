//
//  ContentView.swift
//  WagnerMeter_TestJob
//
//  Created by Ma. Jomalyn O. Espena on 1/28/25.
//

import UIKit
import SwiftUI

var username: String = ""

struct ContentView: View {
    
    var body: some View {
        MainView()
        
            .onAppear {
                if isFirstLaunch() {
                    let alert = UIAlertController(title: "Already a User?", message: nil, preferredStyle: .alert)
                    alert.addTextField { textField in
                        textField.placeholder = "Enter username"
                    }
                    alert.addAction(UIAlertAction(title: "Start Game", style: .default) { _ in
                        if let text = alert.textFields?.first?.text, !text.isEmpty {
                            username = text
                            saveUsername(text)
                            setFirstLaunch()
                        }
                    })
                    if let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                       let window = scene.windows.first {
                        window.rootViewController?.present(alert, animated: true)
                    }
                } else {
                    username = loadUsername()
                }
            }
    }
    
    
    
}

#Preview {
    ContentView()
}
