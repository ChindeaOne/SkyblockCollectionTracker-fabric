# Frequently Asked Questions

**Q: How do I track a collection?**  
**A:** Use `/sct track <collection>`.

---

**Q: How do I track multiple collections at once?**  
**A:** Use `/sct track <collection1> <collection2> ...`.

---

**Q: Why do I get prompted to input some values when starting tracking?**  
**A:** Your Collection API is disabled or the backend rate limit has been reached.

---

**Q: Why doesn't my collection tracker update?**  
**A:** Either another mod is hiding Hypixel's sack messages, or you have **API Tracking** enabled, which updates the tracker approximately every 5 minutes.

---

**Q: Why isn't the tracker 100% accurate?**  
**A:** SCT relies on Hypixel's API and in-game messages. API Tracking updates more slowly than your live gains, while Sack Tracking can miss items from certain sources (e.g. mineshaft corpses) that don't go directly into sacks.

---

**Q: How do I change from NPC to Bazaar prices, or from Instant Sell to Instant Buy?**  
**A:** Either open chat while tracking and use the clickable buttons below the tracker, or change the option under the **Bazaar** category in the config.

---

**Q: How do I open the config?**  
**A:** Use `/sct`, or click the Config button in Mod Menu if you have it installed.

---

**Q: Why does my pickaxe/axe ability notification appear too early?**  
**A:** This can happen if:
- Your lobby is lagging.
- Your Core/Center of the Mountain/Forest level is configured incorrectly.
- Hypixel changed an ability's cooldown or duration.
- A new cooldown reduction method isn't supported by the current version of the SCT.

You can reduce inaccurate timers by enabling **Server Lag Protection** under **Misc**.

---

**Q: I added Mining/Foraging stats to the Stats widget, but the overlay doesn't detect them. How do I fix this?**  
**A:** Make sure the stats are both **enabled** and **visible** in the **STATS** widget. If there isn't enough space, enable the third stats column or disable other widgets in the `/widget` menu.

---

**Q: Why can't I see my Coleweight/Farming Weight rank in chat/name tag?**  
**A:** Only players in the top 1000 of the respective leaderboard have their rank displayed. If you should be in the top 1000 but still don't see it, the leaderboard API was unavailable and the backend had no previously cached leaderboard data.