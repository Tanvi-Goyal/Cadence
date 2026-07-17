import SwiftUI
import WebKit
import Shared

/// Fetches the wger muscle diagram for a muscle name and renders it. SwiftUI `AsyncImage` can't draw
/// SVG, so we composite the base body silhouette + the highlighted-muscle overlay in a WKWebView
/// (dependency-free). Shows nothing until fetched (best-effort; needs network the first time).
struct MuscleDiagramView: View {
    let muscleName: String
    @State private var diagram: MuscleDiagram?

    var body: some View {
        Group {
            if let d = diagram {
                VStack(alignment: .leading, spacing: 4) {
                    SVGLayerWebView(baseUrl: d.baseUrl, overlayUrl: d.overlayUrl)
                        .frame(maxWidth: .infinity)
                        .frame(height: 220)
                        .background(Color(.secondarySystemBackground))
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                    Text("Muscle diagram: wger.de · CC-BY-SA")
                        .font(.caption2).foregroundColor(.secondary)
                }
            }
        }
        .task { diagram = try? await KoinIosKt.muscleDiagram(muscleName: muscleName) }
    }
}

/// Layers two remote SVGs (base + overlay) at identical bounds — they share wger's canvas, so they align.
private struct SVGLayerWebView: UIViewRepresentable {
    let baseUrl: String
    let overlayUrl: String

    func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView(frame: .zero, configuration: WKWebViewConfiguration())
        webView.isOpaque = false
        webView.backgroundColor = .clear
        webView.scrollView.isScrollEnabled = false
        return webView
    }

    func updateUIView(_ webView: WKWebView, context: Context) {
        let html = """
        <!doctype html><html><head>
        <meta name='viewport' content='width=device-width, initial-scale=1'>
        <style>html,body{margin:0;height:100%;background:transparent}
        .wrap{position:relative;width:100%;height:100%}
        .wrap img{position:absolute;inset:0;width:100%;height:100%;object-fit:contain}</style>
        </head><body><div class='wrap'>
        <img src='\(baseUrl)'/><img src='\(overlayUrl)'/>
        </div></body></html>
        """
        webView.loadHTMLString(html, baseURL: URL(string: "https://wger.de/"))
    }
}
