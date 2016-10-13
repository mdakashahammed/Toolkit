### ===========================================================
# title_notifier.js
# ===========================================================
# Copyright 2014 Rafael Oshiro.
# http://www.frontendjournal.com
#
# Lightweight, dependency-free javascript library to dinamically
# show the number of unread notifications in your webpage title.
#
# https://github.com/roshiro/TitleNotifier.js
# ==========================================================
###

do ->
  title = document.getElementsByTagName('title')[0]
  notificationTotal = 0
  patt = /\(\d*\) /

  updateTitle = ->
    if notificationTotal == 0
      title.text = title.text.replace(patt, '')
      return
    if patt.exec(title.text)
      title.text = title.text.replace(patt, '(' + notificationTotal + ') ')
    else
      title.text = '(' + notificationTotal + ') ' + title.text
    return

  isNumber = (n) ->
    !isNaN(parseFloat(n)) and isFinite(n)

  ###*
  # TitleNotifier Namespace.
  ###

  window.titlenotifier =
    add: (value) ->
      if typeof value == 'undefined'
        value = 1
      notificationTotal += parseInt(value, 10)
      updateTitle()
      return
    sub: (value) ->
      if typeof value == 'undefined'
        value = 1
      value = parseInt(value, 10)
      if notificationTotal == 0
        return
      else if value > notificationTotal
        notificationTotal = 0
      else
        notificationTotal -= parseInt(value, 10)
      updateTitle()
      return
    set: (value) ->
      if !isNumber(value) or value < 0
        return
      notificationTotal = parseInt(value, 10)
      updateTitle()
      return
    reset: ->
      notificationTotal = 0
      updateTitle()
      return
  return

# ---
# generated by js2coffee 2.2.0



