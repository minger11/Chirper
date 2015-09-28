#Messages array
window.message = []

#Posts a text string to the server via a XMLHttp request
window.postMessage = (state) ->

  #sets the path of the server based on the location and the inputted message
  path = "http://#{window.location.host}/message"

  #creates the request
  xhr = new XMLHttpRequest()

  console.log("posting message")

  #Opens the connection, sends credentials and sets the content type to json
  xhr.open("POST", path, true)
  xhr.withCredentials = true
  xhr.setRequestHeader("Content-Type", "application/json")

  #creates the json object
  m = {
    message: state.post
  }

  #stringifies the object
  json = JSON.stringify(m)

  #sends the json string to the server
  xhr.send(json)

#gets a text string from the server via XMLHttp request
window.getMessage = (state) ->

  #sets the path of the server based on the server location and the inputted search string
  path = "http://#{window.location.host}/message?n=" + state.searchString

  #creates the request
  xhr = new XMLHttpRequest()

  xhr.onreadystatechange = () ->
    if xhr.readyState == 4

      #parses the response text to a json array
      arr = JSON.parse(xhr.responseText)

      #sets the array to the variable window.message
      window.message = arr

      console.log(arr)

      #rerenders the message app
      rerender()

  console.log("asking for message")

  #opens the connection, requests with credentials
  xhr.open("GET", path, true)
  xhr.withCredentials = true
  xhr.send()

#opens a websocket to receive data from the server
window.getSocket = (state) ->

  #creates the websocket based on the server location and the inputted search string
  websocket = new WebSocket("ws://#{window.location.host}/ws?topic=" + state.searchString);

  #upon receiving data
  websocket.onmessage = (msg) ->

    console.log("Received a message over the websocket:")
    console.log(msg)
    console.log("---")

    #parse the data to json
    json = JSON.parse(msg.data)

    #assign the data to the window.json variable
    window.message = json

    #rerender the message app
    rerender()
