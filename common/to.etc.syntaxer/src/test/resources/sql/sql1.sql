sum (
     case
        when Consignment!.weight is null or Consignment!.weight <= 0
        then ( Parameterwaardenumeriek('PACKAGINGWEIGHT', Consignment!.packaging, Consignment!.pickupDate) * Consignment!.quantity)
        else Consignment!.weight
     end
     * Consignment!.greatCircleDistance
    )
