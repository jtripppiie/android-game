#!/usr/bin/env python3
"""Serve a Godot web export on localhost with browser isolation headers."""
import argparse
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer


class GameHandler(SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_header("Cross-Origin-Opener-Policy", "same-origin")
        self.send_header("Cross-Origin-Embedder-Policy", "require-corp")
        self.send_header("Cache-Control", "no-store")
        super().end_headers()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("directory", help="Folder containing the exported index.html")
    parser.add_argument("--port", type=int, default=8060)
    args = parser.parse_args()
    server = ThreadingHTTPServer(
        ("127.0.0.1", args.port), partial(GameHandler, directory=args.directory)
    )
    print(f"Moto Thrash: http://localhost:{args.port}/", flush=True)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        server.server_close()
