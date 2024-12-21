import main
import os

from flask import Flask

app = Flask(__name__)


@app.route("/asset=<asset>")
def get_response(asset):
    asset = str(asset).upper()

    try:
        reportExists = os.path.exists(f"../../reports/{asset}.md")
    except:
        print("No pre-existing report")

    if not reportExists:
        main.run(asset)
        os.rename("report.md", f"../../reports/{asset}.md")

    file = open(f"../../reports/{asset}.md")
    response = file.read()

    return {"input": asset, "response": response}


app.run()
