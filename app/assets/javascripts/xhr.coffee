
console.log("I was loaded")

window.message = []

window.user = ""

window.received = []

window.postMessage = (state) ->

  path = "http://#{window.location.host}/message"

  xhr = new XMLHttpRequest()

  console.log("posting message")

  xhr.open("POST", path, true)
  xhr.withCredentials = true
  xhr.setRequestHeader("Content-Type", "application/json")

  m = {
    message: state.post
  }

  json = JSON.stringify(m)

  xhr.send(json)

window.getMessage = (state) ->

  path = "http://#{window.location.host}/message?n=" + state.searchString

  xhr = new XMLHttpRequest()

  xhr.onreadystatechange = () ->
    if xhr.readyState == 4
      arr = JSON.parse(xhr.responseText)
      window.message = arr
      console.log(arr)
      rerender()

  console.log("asking for message")
  xhr.open("GET", path, true)
  xhr.withCredentials = true
  xhr.send()

window.getSocket = (state) ->

  websocket = new WebSocket("ws://#{window.location.host}/websocket?topic=" + state.searchString);

  websocket.onmessage = (msg) ->
    console.log("Received a message over the websocket:")
    console.log(msg)
    console.log("---")
    json = JSON.parse(msg.data)
    window.message = json
    rerender()
