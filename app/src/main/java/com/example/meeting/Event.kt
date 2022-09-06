package com.example.meeting

import java.io.Serializable
import java.util.*

class Event (val eventId: String ?= null,
             val title: String ?= null,
             val description: String ?= null,
             val category: String ?= null,
             val address: String ?= null,
             val userId: String ?= null,
             val date: String ?= null,
             val time: String ?= null,
             val imageLocation: String ?= null,
             val participants : ArrayList<String> ?= null) : Serializable