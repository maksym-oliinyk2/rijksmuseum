//
//  ios_appApp.swift
//  ios-app
//
//  Created by Maksym Oliinyk2 on 17.03.2026.
//

import SwiftUI
import RijksmuseumLib

@main
struct ios_appApp: App {
    
    var body: some Scene {
        WindowGroup {
            ComposeViewController()
                .ignoresSafeArea(.all)
        }
    }
}

struct ComposeViewController: UIViewControllerRepresentable {

    func makeUIViewController(context: Context) -> UIViewController {
        App_appleKt.appController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
