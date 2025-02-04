//
//  Helpers.swift
//  WagnerMeter_TestJob
//
//  Created by Ma. Jomalyn O. Espena on 2/3/25.
//

import UIKit

func isFirstLaunch() -> Bool {
    let defaults = UserDefaults.standard
    if defaults.bool(forKey: "hasLaunched") {
        return false
    } else {
        defaults.set(true, forKey: "hasLaunched")
        return true
    }
}

func setFirstLaunch(){
    let defaults = UserDefaults.standard
    defaults.set(true, forKey: "hasLaunched")
}

func generateUserID() -> String {
    let letters = Array("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ")
    let randomLetters = (0..<3).map { _ in String(letters.randomElement()!) }.joined()
    let randomDigits = Int.random(in: 100...999)
    return randomLetters + String(randomDigits)
}

func saveUsername(_ username: String) {
    let defaults = UserDefaults.standard
    defaults.set(username, forKey: "username")
}

func loadUsername() -> String {
    let defaults = UserDefaults.standard
    if let username = defaults.string(forKey: "username"), !username.isEmpty {
        return username
    }
    let newUsername = generateUserID()
    saveUsername(newUsername)
    return newUsername
}
