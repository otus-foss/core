package com.otus.designsystem.shared.people.elements

import com.otus.designsystem.resources.Res
import com.otus.designsystem.resources.symbol_ac_unit
import com.otus.designsystem.resources.symbol_account_balance
import com.otus.designsystem.resources.symbol_anchor
import com.otus.designsystem.resources.symbol_beach_access
import com.otus.designsystem.resources.symbol_bolt
import com.otus.designsystem.resources.symbol_brush
import com.otus.designsystem.resources.symbol_build
import com.otus.designsystem.resources.symbol_cake
import com.otus.designsystem.resources.symbol_castle
import com.otus.designsystem.resources.symbol_change_history_fill
import com.otus.designsystem.resources.symbol_circle_fill
import com.otus.designsystem.resources.symbol_cloud
import com.otus.designsystem.resources.symbol_code
import com.otus.designsystem.resources.symbol_computer
import com.otus.designsystem.resources.symbol_cruelty_free
import com.otus.designsystem.resources.symbol_diamond
import com.otus.designsystem.resources.symbol_directions_bike
import com.otus.designsystem.resources.symbol_directions_car
import com.otus.designsystem.resources.symbol_eco
import com.otus.designsystem.resources.symbol_favorite_fill
import com.otus.designsystem.resources.symbol_fitness_center
import com.otus.designsystem.resources.symbol_flight
import com.otus.designsystem.resources.symbol_forest
import com.otus.designsystem.resources.symbol_group
import com.otus.designsystem.resources.symbol_handshake
import com.otus.designsystem.resources.symbol_headphones
import com.otus.designsystem.resources.symbol_hexagon_fill
import com.otus.designsystem.resources.symbol_hiking
import com.otus.designsystem.resources.symbol_home
import com.otus.designsystem.resources.symbol_lightbulb
import com.otus.designsystem.resources.symbol_local_cafe
import com.otus.designsystem.resources.symbol_local_fire_department
import com.otus.designsystem.resources.symbol_local_florist
import com.otus.designsystem.resources.symbol_local_pizza
import com.otus.designsystem.resources.symbol_mail
import com.otus.designsystem.resources.symbol_medical_services
import com.otus.designsystem.resources.symbol_menu_book
import com.otus.designsystem.resources.symbol_mic
import com.otus.designsystem.resources.symbol_music_note
import com.otus.designsystem.resources.symbol_nightlight
import com.otus.designsystem.resources.symbol_palette
import com.otus.designsystem.resources.symbol_pentagon_fill
import com.otus.designsystem.resources.symbol_person
import com.otus.designsystem.resources.symbol_pets
import com.otus.designsystem.resources.symbol_photo_camera
import com.otus.designsystem.resources.symbol_piano
import com.otus.designsystem.resources.symbol_pool
import com.otus.designsystem.resources.symbol_psychology
import com.otus.designsystem.resources.symbol_public
import com.otus.designsystem.resources.symbol_restaurant
import com.otus.designsystem.resources.symbol_rocket_launch
import com.otus.designsystem.resources.symbol_sailing
import com.otus.designsystem.resources.symbol_savings
import com.otus.designsystem.resources.symbol_school
import com.otus.designsystem.resources.symbol_science
import com.otus.designsystem.resources.symbol_shield
import com.otus.designsystem.resources.symbol_shopping_bag
import com.otus.designsystem.resources.symbol_smartphone
import com.otus.designsystem.resources.symbol_spa
import com.otus.designsystem.resources.symbol_sports_basketball
import com.otus.designsystem.resources.symbol_sports_esports
import com.otus.designsystem.resources.symbol_sports_soccer
import com.otus.designsystem.resources.symbol_square_fill
import com.otus.designsystem.resources.symbol_star_fill
import com.otus.designsystem.resources.symbol_storefront
import com.otus.designsystem.resources.symbol_terminal
import com.otus.designsystem.resources.symbol_train
import com.otus.designsystem.resources.symbol_volunteer_activism
import com.otus.designsystem.resources.symbol_water_drop
import com.otus.designsystem.resources.symbol_work
import com.otus.designsystem.resources.symbol_workspace_premium
import org.jetbrains.compose.resources.DrawableResource

/**
 * The icons a user can put on their avatar, in picker order, ending with plain shapes. Apps store
 * the key and look it up here, so keep keys stable; a key that is gone shows the color alone.
 */
public object AvatarSymbols {
    public val icons: Map<String, DrawableResource> =
        mapOf(
            "person" to Res.drawable.symbol_person,
            "group" to Res.drawable.symbol_group,
            "work" to Res.drawable.symbol_work,
            "home" to Res.drawable.symbol_home,
            "mail" to Res.drawable.symbol_mail,
            "school" to Res.drawable.symbol_school,
            "books" to Res.drawable.symbol_menu_book,
            "code" to Res.drawable.symbol_code,
            "terminal" to Res.drawable.symbol_terminal,
            "computer" to Res.drawable.symbol_computer,
            "phone" to Res.drawable.symbol_smartphone,
            "science" to Res.drawable.symbol_science,
            "mind" to Res.drawable.symbol_psychology,
            "build" to Res.drawable.symbol_build,
            "idea" to Res.drawable.symbol_lightbulb,
            "shop" to Res.drawable.symbol_storefront,
            "bank" to Res.drawable.symbol_account_balance,
            "money" to Res.drawable.symbol_savings,
            "shopping" to Res.drawable.symbol_shopping_bag,
            "health" to Res.drawable.symbol_medical_services,
            "care" to Res.drawable.symbol_volunteer_activism,
            "deal" to Res.drawable.symbol_handshake,
            "palette" to Res.drawable.symbol_palette,
            "brush" to Res.drawable.symbol_brush,
            "camera" to Res.drawable.symbol_photo_camera,
            "music" to Res.drawable.symbol_music_note,
            "headphones" to Res.drawable.symbol_headphones,
            "mic" to Res.drawable.symbol_mic,
            "piano" to Res.drawable.symbol_piano,
            "games" to Res.drawable.symbol_sports_esports,
            "soccer" to Res.drawable.symbol_sports_soccer,
            "basketball" to Res.drawable.symbol_sports_basketball,
            "fitness" to Res.drawable.symbol_fitness_center,
            "swim" to Res.drawable.symbol_pool,
            "hiking" to Res.drawable.symbol_hiking,
            "bike" to Res.drawable.symbol_directions_bike,
            "food" to Res.drawable.symbol_restaurant,
            "pizza" to Res.drawable.symbol_local_pizza,
            "cake" to Res.drawable.symbol_cake,
            "coffee" to Res.drawable.symbol_local_cafe,
            "pets" to Res.drawable.symbol_pets,
            "rabbit" to Res.drawable.symbol_cruelty_free,
            "nature" to Res.drawable.symbol_eco,
            "forest" to Res.drawable.symbol_forest,
            "flower" to Res.drawable.symbol_local_florist,
            "spa" to Res.drawable.symbol_spa,
            "beach" to Res.drawable.symbol_beach_access,
            "sailing" to Res.drawable.symbol_sailing,
            "anchor" to Res.drawable.symbol_anchor,
            "travel" to Res.drawable.symbol_flight,
            "train" to Res.drawable.symbol_train,
            "car" to Res.drawable.symbol_directions_car,
            "world" to Res.drawable.symbol_public,
            "castle" to Res.drawable.symbol_castle,
            "cloud" to Res.drawable.symbol_cloud,
            "snow" to Res.drawable.symbol_ac_unit,
            "water" to Res.drawable.symbol_water_drop,
            "fire" to Res.drawable.symbol_local_fire_department,
            "night" to Res.drawable.symbol_nightlight,
            "bolt" to Res.drawable.symbol_bolt,
            "rocket" to Res.drawable.symbol_rocket_launch,
            "shield" to Res.drawable.symbol_shield,
            "diamond" to Res.drawable.symbol_diamond,
            "crown" to Res.drawable.symbol_workspace_premium,
            "circle" to Res.drawable.symbol_circle_fill,
            "square" to Res.drawable.symbol_square_fill,
            "triangle" to Res.drawable.symbol_change_history_fill,
            "pentagon" to Res.drawable.symbol_pentagon_fill,
            "hexagon" to Res.drawable.symbol_hexagon_fill,
            "star" to Res.drawable.symbol_star_fill,
            "heart" to Res.drawable.symbol_favorite_fill,
        )
}
